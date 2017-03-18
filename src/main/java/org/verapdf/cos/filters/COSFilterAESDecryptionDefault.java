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
package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * This filter decrypts data using AES cipher.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterAESDecryptionDefault extends ASBufferedInFilter {

    private static final byte[] SALT_BYTES = new byte[]{0x73, 0x41, 0x6C, 0x54};

    private Cipher aes;
    private int decryptedPointer;
    private byte[] decryptedBytes;
    private boolean decryptingCOSStream;
    private boolean haveReadStream;

    /**
     * Constructor.
     *
     * @param stream        is stream with encrypted data.
     * @param objectKey     contains object and generation numbers from object
     *                      identifier for object that is being decrypted. If it is
     *                      direct object, objectKey is taken from indirect object
     *                      that contains it.
     * @param encryptionKey is encryption key that is calculated from user
     *                      password and encryption dictionary.
     */
    public COSFilterAESDecryptionDefault(ASInputStream stream, COSKey objectKey,
                                         byte[] encryptionKey, boolean decryptingCOSStream)
            throws IOException, GeneralSecurityException {
        super(stream);
        initAES(objectKey, encryptionKey);
        decryptedBytes = new byte[0];
        decryptedPointer = 0;
        this.decryptingCOSStream = decryptingCOSStream;
        this.haveReadStream = false;
    }

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (this.getInputStream() == null) {
            return -1;
        }
        if(decryptingCOSStream && !haveReadStream) {
            this.getInputStream().skip(16);
            this.haveReadStream = true;
        }
        int readDecrypted = this.readFromDecryptedBytes(buffer, size);
        if (readDecrypted != -1) {
            return readDecrypted;
        }

        if (this.bufferSize() <= 0) {
            int bytesFed = (int) this.feedBuffer(getBufferCapacity());
            if (bytesFed == -1) {
                return -1;
            }
        }
        byte[] encData = new byte[BF_BUFFER_SIZE];
        int encDataLength = this.bufferPopArray(encData, BF_BUFFER_SIZE);
        try {
            this.decryptedBytes = this.aes.update(encData, 0, encDataLength);
            byte[] fin = this.aes.doFinal();
            this.decryptedBytes = concatenate(this.decryptedBytes,
                    decryptedBytes.length, fin, fin.length);
            return this.readFromDecryptedBytes(buffer, size);
        } catch (GeneralSecurityException e) {
            throw new IOException("Can't decrypt AES data.");
        }
    }

    private void initAES(COSKey objectKey, byte[] encryptionKey)
            throws IOException, GeneralSecurityException {
        byte[] objectKeyDigest =
                COSFilterRC4DecryptionDefault.getObjectKeyDigest(objectKey);
        byte[] encWithObjectKey = concatenate(encryptionKey, encryptionKey.length,
                objectKeyDigest, objectKeyDigest.length);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(concatenate(encWithObjectKey, encWithObjectKey.length,
                SALT_BYTES, SALT_BYTES.length));
        byte[] resultEncryptionKey = md5.digest();
        int keyLength = Math.min(COSFilterRC4DecryptionDefault.MAXIMAL_KEY_LENGTH,
                resultEncryptionKey.length);
        SecretKey key = new SecretKeySpec(
                Arrays.copyOf(resultEncryptionKey, keyLength), "AES");
        IvParameterSpec initializingVector = new
                IvParameterSpec(getAESInitializingVector());
        this.aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        this.aes.init(Cipher.DECRYPT_MODE, key, initializingVector);
    }

    private byte[] getAESInitializingVector() throws IOException {
        byte[] initVector = new byte[16];
        if (this.getInputStream() == null ||
                this.getInputStream().read(initVector, 16) != 16) {
            throw new IOException("Can't initialize AES cipher: AES initializing" +
                    " vector is not fully read.");
        }
        return initVector;
    }

    private int readFromDecryptedBytes(byte[] buffer, int size) {
        if (decryptedBytes.length == decryptedPointer) {
            return -1;
        }
        int actualRead = Math.min(size, decryptedBytes.length - decryptedPointer);
        System.arraycopy(decryptedBytes, decryptedPointer, buffer, 0, actualRead);
        decryptedPointer += actualRead;
        return actualRead;
    }
}
