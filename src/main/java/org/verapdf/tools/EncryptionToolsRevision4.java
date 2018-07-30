/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Contains methods for encryption and decryption of PDF files.
 *
 * @author Sergey Shemyakov
 */
public class EncryptionToolsRevision4 {

    private static final int PADDED_PASSWORD_LENGTH = 32;
    private static final int AMOUNT_OF_REPEATS_MD5 = 50;
    private static final int AMOUNT_OF_REPEATS_RC4 = 19;
    private static final int U_LENGTH = 16;
    private static final byte[] DEFAULT_PADDING_STRING = new byte[]{
            0x28, (byte) 0xBF, 0x4E, 0x5E, 0x4E, 0x75, (byte) 0x8A, 0x41,
            0x64, 0x00, 0x4E, 0x56, (byte) 0xFF, (byte) 0xFA, 0x01, 0x08,
            0x2E, 0x2E, 0x00, (byte) 0xB6, (byte) 0xD0, 0x68, 0x3E, (byte) 0x80,
            0x2F, 0x0C, (byte) 0xA9, (byte) 0xFE, 0x64, 0x53, 0x69, 0x7A
    };
    private static final byte[] FF_STRING = new byte[]{-1, -1, -1, -1};

    private EncryptionToolsRevision4() {
    }

    /**
     * Method computes encryption key for given data as specified in 7.6.3.3.
     * Algorithm 2 of PDF32000_2008.
     *
     * @param password            is password string.
     * @param o                   is O value of encryption dict for standard
     *                            security handler.
     * @param p                   is P value of encryption dict for standard
     *                            security handler.
     * @param id                  is the value of the ID entry in the document’s
     *                            trailer dict.
     * @param revision            is R value of encryption dict for standard
     *                            security handler.
     * @param metadataIsEncrypted is true if metadata in file is encrypted.
     * @param length              is value of Length in encryption dict.
     * @return encryption key.
     */
    public static byte[] computeEncryptionKey(String password, byte[] o, int p,
                                              byte[] id, int revision,      // id is first ID in trailer
                                              boolean metadataIsEncrypted,
                                              int length) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(getPaddedPassword(password));
        md5.update(o);
        md5.update(intToBytesLowOrderFirst(p));
        md5.update(id);
        if (revision >= 4 && !metadataIsEncrypted) {
            md5.update(FF_STRING);
        }
        byte[] res = md5.digest();
        if (revision >= 3) {
            for (int i = 0; i < AMOUNT_OF_REPEATS_MD5; ++i) {
                md5.reset();
                md5.update(Arrays.copyOf(res, length / 8));
                res = md5.digest();
            }
        }
        //todo: discuss cutting for 5 bits
        return Arrays.copyOf(res, length / 8);
    }

    /**
     * Method computes O value in standard encryption dict for standard security
     * handler as specified in 7.6.3.4. Algorithm 3 of PDF32000_2008.
     *
     * @param ownerPassword is owner password.
     * @param revision      is R value of encryption dict for standard security
     *                      handler.
     * @param length        is value of Length in encryption dict.
     * @param userPassword  is user password.
     * @return value of O for encryption dict for standard security handler.
     */
    public static byte[] computeOValue(String ownerPassword, int revision, int length,
                                       String userPassword) throws NoSuchAlgorithmException {
        if (ownerPassword == null) {
            ownerPassword = userPassword;
        }
        byte[] password = getPaddedPassword(ownerPassword);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(password);
        password = md5.digest();
        if (revision >= 3) {
            for (int i = 0; i < AMOUNT_OF_REPEATS_MD5; ++i) {
                md5.reset();
                md5.update(password);
                password = md5.digest();
            }
        }
        int actualLength = revision >= 3 ? length / 8 : 5;
        RC4Encryption rc4 = new RC4Encryption(Arrays.copyOf(password, actualLength));
        byte[] rc4Result = rc4.process(getPaddedPassword(ownerPassword));
        if (revision >= 3) {
            for (int i = 1; i <= AMOUNT_OF_REPEATS_RC4; i++) {
                rc4 = new RC4Encryption(modifyEncryptionKeyWithCounter(
                        Arrays.copyOf(password, actualLength), i));
                rc4Result = rc4.process(rc4Result);
            }
        }
        return rc4Result;
    }

    /**
     * Method computes U value in standard encryption dict for standard security
     * handler as specified in 7.6.3.4. Algorithm 4 and 5 of PDF32000_2008.
     *
     * @param password            is user password.
     * @param o                   is O value of encryption dict for standard
     *                            security handler.
     * @param p                   is P value of encryption dict for standard
     *                            security handler.
     * @param id                  is the value of the ID entry in the document’s
     *                            trailer dict.
     * @param revision            is R value of encryption dict for standard
     *                            security handler.
     * @param metadataIsEncrypted is true if metadata in file is encrypted.
     * @param length              is value of Length in encryption dict.
     * @return value of U for encryption dict for standard security handler.
     */
    public static byte[] computeUValue(String password, byte[] o, int p,
                                       byte[] id, int revision,
                                       boolean metadataIsEncrypted,
                                       int length) throws NoSuchAlgorithmException {
        if (revision == 2) {
            return computeUValueRevision2(password, o, p, id,
                    metadataIsEncrypted, length);
        }
        byte[] key = computeEncryptionKey(password, o, p, id, revision,
                metadataIsEncrypted, length);
        RC4Encryption rc4 = new RC4Encryption(key);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(DEFAULT_PADDING_STRING);
        md5.update(id);
        byte[] md5Result = md5.digest();
        byte[] rc4Result = rc4.process(md5Result);
        for (int i = 1; i <= AMOUNT_OF_REPEATS_RC4; i++) {
            rc4 = new RC4Encryption(modifyEncryptionKeyWithCounter(
                    key, i));
            rc4Result = rc4.process(rc4Result);
        }
        return rc4Result;
    }

    /**
     * Authenticates user password and returns encryption key if password is
     * correct.
     *
     * @param password            is string to be checked.
     * @param o                   is O value of encryption dict for standard
     *                            security handler.
     * @param p                   is P value of encryption dict for standard
     *                            security handler.
     * @param id                  is the value of the ID entry in the document’s
     *                            trailer dict.
     * @param revision            is R value of encryption dict for standard
     *                            security handler.
     * @param metadataIsEncrypted is true if metadata in file is encrypted.
     * @param length              is value of Length in encryption dict.
     * @param u                   is U value of encryption dict for standard
     *                            security handler.
     * @return null if password is incorrect and encryption key if it is
     * correct.
     */
    public static byte[] authenticateUserPassword(String password, byte[] o, int p,
                                                  byte[] id, int revision,
                                                  boolean metadataIsEncrypted,
                                                  int length, byte[] u) throws NoSuchAlgorithmException {
        byte[] uValue = computeUValue(password, o, p, id, revision,
                metadataIsEncrypted, length);
        if (revision >= 3) {
            u = Arrays.copyOf(u, U_LENGTH);
        }
        if (Arrays.equals(u, uValue)) {
            return computeEncryptionKey(password, o, p, id, revision,
                    metadataIsEncrypted, length);
        } else {
            return null;
        }
    }

    private static byte[] computeUValueRevision2(String password, byte[] o, int p,
                                                 byte[] id,
                                                 boolean metadataIsEncrypted,
                                                 int length) throws NoSuchAlgorithmException {
        byte[] key = computeEncryptionKey(password, o, p, id, 2,
                metadataIsEncrypted, length);
        RC4Encryption rc4 = new RC4Encryption(key);
        return rc4.process(DEFAULT_PADDING_STRING);
    }

    private static byte[] getPaddedPassword(String password) {
        if (password == null) {
            password = "";
        }
        byte[] psw;
        try {
            psw = password.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            psw = password.getBytes();
        }
        byte[] res = new byte[PADDED_PASSWORD_LENGTH];
        if (psw.length > PADDED_PASSWORD_LENGTH) {
            System.arraycopy(psw, 0, res, 0, PADDED_PASSWORD_LENGTH);
        } else {
            System.arraycopy(psw, 0, res, 0, psw.length);
            for (int i = 0; i < PADDED_PASSWORD_LENGTH - psw.length; ++i) {
                res[i + psw.length] = DEFAULT_PADDING_STRING[i];
            }
        }
        return res;
    }

    /**
     * Represents given integer as byte array where low-order bytes go first.
     *
     * @param p is integer.
     * @return array of 4 bytes that represent this number as unsigned 32-bit
     * integer, low-order bytes go first.
     */
    public static byte[] intToBytesLowOrderFirst(long p) {
        byte[] res = new byte[4];
        for (int i = 0; i < 4; ++i) {
            byte b = (byte) (p & 0xFF);
            p >>= 8;
            res[i] = b;
        }
        return res;
    }

    private static byte[] modifyEncryptionKeyWithCounter(byte[] key, int counter) {
        byte[] res = new byte[key.length];
        for (int i = 0; i < key.length; ++i) {
            res[i] = (byte) (key[i] ^ counter);
        }
        return res;
    }
}
