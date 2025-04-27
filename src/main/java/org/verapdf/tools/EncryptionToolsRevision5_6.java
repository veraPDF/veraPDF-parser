/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.tools;

import org.verapdf.as.filters.io.ASBufferedInFilter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author Sergey Shemyakov
 */
public class EncryptionToolsRevision5_6 {

    /**
     * Implementation of algorithm 2.A: Retrieving the file encryption key from
     * an encrypted document in order to decrypt it (revision 6 and later) as
     * described in PDF-2.0 specification.
     *
     * @param password is UTF-8 representation of password.
     * @param o        is value of O key.
     * @param u        is value of U key.
     * @param oe       is value of OE key. At least one of oe, ue should not be null.
     * @param ue       is value of UE key. At least one of oe, ue should not be null.
     * @return calculated file encryption key.
     * @throws GeneralSecurityException if password is not valid or error with
     *                                  AES-256 happens.
     */
    public static byte[] getFileEncryptionKey(byte[] password, byte[] o, byte[] u,
                                              byte[] oe, byte[] ue, long revision) throws GeneralSecurityException {
        byte[] hash = computeHash(password, getValSaltFromString(o), u, true, revision);
        boolean isUser = false;

        if (!Arrays.equals(hash, getHashValueFromString(o))) {
            isUser = true;
            hash = computeHash(password, getValSaltFromString(u), u, false, revision);
            if (!Arrays.equals(hash, getHashValueFromString(u))) {
                throw new GeneralSecurityException("Incorrect password: failed check of owner hash.");
            }
        }

        byte[] e;
        byte[] stringForSalt;
        if (isUser) {
            stringForSalt = u;
            e = ue;
        } else {
            stringForSalt = o;
            e = oe;
        }

        byte[] res = null;
        if (e != null) {
            byte[] aesKey = computeHash(password, getKeySaltFromString(stringForSalt), u, !isUser, revision);
            enableAES256();
            SecretKey key = new SecretKeySpec(aesKey, "AES");
            Cipher aes = Cipher.getInstance("AES/CBC/NoPadding");
            aes.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
            res = aes.doFinal(e);
        }

        // Maybe checking Perms should be added.

        return res;
    }


    /**
     * Implements algorithm 2.B: Computing a hash (revision 6 and later),
     * introduced in PDF-2.0.
     *
     * @param salt                    is UTF-8 salt
     * @param u                       is 48 byte user key.
     * @param isCheckingOwnerPassword is true if hash is being calculated for
     *                                checking the owner password or creating
     *                                the owner key.
     * @return computed hash.
     */
    private static byte[] computeHash(byte[] password, byte[] salt, byte[] u,
                                               boolean isCheckingOwnerPassword, long revision)
            throws GeneralSecurityException {
        byte[] hashInput = ASBufferedInFilter.concatenate(password,
                password.length, salt, 8);
        if (isCheckingOwnerPassword) {
            hashInput = ASBufferedInFilter.concatenate(hashInput, hashInput.length, u, u.length);
        }
        byte[] k = getSHAHash(256, hashInput);
        if (revision > 5) {
            int rounds = 0;
            while (true) {
                byte[] sequence = ASBufferedInFilter.concatenate(password, password.length, k, k.length);
                if (isCheckingOwnerPassword) {
                    sequence = ASBufferedInFilter.concatenate(sequence, sequence.length, u, u.length);
                }
                byte[] k1 = repeatString(sequence, 64);

                IvParameterSpec initializingVector = new IvParameterSpec(Arrays.copyOfRange(k, 16, 32));
                SecretKey key = new SecretKeySpec(Arrays.copyOf(k, 16), "AES");
                Cipher aes = Cipher.getInstance("AES/CBC/NoPadding");
                aes.init(Cipher.ENCRYPT_MODE, key, initializingVector);
                byte[] e = aes.doFinal(k1);
                int shaType = getReminderByModulo3(Arrays.copyOf(e, 16));
                k = getSHAHash(shaType == 0 ? 256 : shaType == 1 ? 384 : 512, e);
                if (rounds >= 63 && (e[e.length - 1] & 0xFF) <= rounds - 32) {
                    break;
                }
                rounds++;
            }
        }
        return Arrays.copyOf(k, 32);
    }

    public static void enableAES256() throws GeneralSecurityException {
        if (Cipher.getMaxAllowedKeyLength("AES") < 256) {
            try {   // Allow using of AES with 256-bit key.
                Field restrictedField = Class.forName("javax.crypto.JceSecurity").
                        getDeclaredField("isRestricted");

                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                Field modifiersField = null;
                for (Field field : fields) {
                    if ("modifiers".equals(field.getName())) {
                        modifiersField = field;
                        break;
                    }
                }
                if (modifiersField == null) {
                    throw new IllegalStateException("Field \"modifiers\" is not declared in java.lang.reflect.Field");
                }
                modifiersField.setAccessible(true);
                modifiersField.setInt(restrictedField, restrictedField.getModifiers() & ~Modifier.FINAL);
                modifiersField.setAccessible(false);

                restrictedField.setAccessible(true);
                restrictedField.set(null, Boolean.FALSE);
            } catch (Exception ex) {
                throw new GeneralSecurityException("Can't enable using of 256-bit key for AES encryption", ex);
            }
        }
    }

    private static byte[] repeatString(byte[] string, int amount) {
        byte[] res = new byte[string.length * amount];
        for (int i = 0; i < amount; ++i) {
            System.arraycopy(string, 0, res, i * string.length, string.length);
        }
        return res;
    }

    private static byte[] getSHAHash(int shaNum, byte[] message) throws NoSuchAlgorithmException {
        if (shaNum != 256 && shaNum != 384 && shaNum != 512) {
            throw new IllegalStateException("Can't use SHA-" + shaNum + " hash.");
        }
        String shaMD = "SHA-" + shaNum;
        MessageDigest md = MessageDigest.getInstance(shaMD);
        md.update(message);
        return md.digest();
    }

    private static byte[] getHashValueFromString(byte[] string) {
        return Arrays.copyOf(string, 32);
    }

    private static byte[] getValSaltFromString(byte[] string) {
        return Arrays.copyOfRange(string, 32, 40);
    }

    private static byte[] getKeySaltFromString(byte[] string) {
        return Arrays.copyOfRange(string, 40, 48);
    }

    private static int getReminderByModulo3(byte[] array) {
        int res = 0;
        for (byte b : array) {
            // 256 = 1 mod 3, so x * (256^n) = x mod 3
            res += b & 0xFF;
        }
        return res % 3;
    }
}
