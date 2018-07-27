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

import org.verapdf.as.ASAtom;
import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSKey;
import org.verapdf.tools.EncryptionToolsRevision5_6;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * This filter decrypts data using AES cipher as described in Algorithm 1 of
 * 7.6.2 of ISO 32000:2008.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterAESDecryptionDefault extends ASBufferedInFilter {

    private static final byte[] SALT_BYTES = new byte[]{0x73, 0x41, 0x6C, 0x54};

    private SecretKey key;
    private IvParameterSpec initializingVector;
    private Cipher aes;
    private boolean isDecryptFinished;
    private int decryptedPointer;
    private byte[] decryptedBytes;
    private final boolean decryptingCOSStream;
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
     * @param method        value of CFM key in crypt filter dictionary, should
     *                      be AESV2 or AESV3.
     */
    public COSFilterAESDecryptionDefault(ASInputStream stream, COSKey objectKey,
                                         byte[] encryptionKey, boolean decryptingCOSStream,
                                         ASAtom method)
            throws IOException, GeneralSecurityException {
        super(stream);
        this.decryptingCOSStream = decryptingCOSStream;
        if (method == ASAtom.AESV2) {
            prepareInitAES128(objectKey, encryptionKey);
        } else if (method == ASAtom.AESV3) {
            prepareInitAES256(encryptionKey);
        } else {
            throw new IllegalStateException("Unknown version of AES encryption algorithm");
        }
        this.aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        init();
    }

    private void init() throws GeneralSecurityException {
        this.aes.init(Cipher.DECRYPT_MODE, key, initializingVector);
        this.isDecryptFinished = false;
        this.decryptedBytes = new byte[0];
        this.decryptedPointer = 0;
        this.haveReadStream = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (this.getInputStream() == null) {
            return -1;
        }
        if (decryptingCOSStream && !haveReadStream) {
            this.getInputStream().skip(16);
            this.haveReadStream = true;
        }
        int readDecrypted = this.readFromDecryptedBytes(buffer, size);
        if (readDecrypted != -1 || isDecryptFinished) {
            return readDecrypted;
        }

        try {
            if (this.bufferSize() <= 0) {
                int bytesFed = (int) this.feedBuffer(getBufferCapacity());
                if (bytesFed == -1) {
                    isDecryptFinished = true;
                    this.decryptedBytes = this.aes.doFinal();
                }
            }

            if (!isDecryptFinished) {
                byte[] encData = new byte[BF_BUFFER_SIZE];
                int encDataLength = this.bufferPopArray(encData, BF_BUFFER_SIZE);
                this.decryptedBytes = this.aes.update(encData, 0, encDataLength);
            }
            this.decryptedPointer = 0;

            int read = this.readFromDecryptedBytes(buffer, size);
            return Math.max(read, 0);
        } catch (GeneralSecurityException e) {
            throw new IOException("Can't decrypt AES data.");
        }
    }

    private void prepareInitAES128(COSKey objectKey, byte[] encryptionKey)
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
        this.key = new SecretKeySpec(
                Arrays.copyOf(resultEncryptionKey, keyLength), "AES");
        this.initializingVector = new
                IvParameterSpec(getAESInitializingVector());
    }

    private void prepareInitAES256(byte[] encryptionKey) throws IOException,
            GeneralSecurityException {
        EncryptionToolsRevision5_6.enableAES256();
        this.key = new SecretKeySpec(
                Arrays.copyOf(encryptionKey, 32), "AES");
        this.initializingVector = new
                IvParameterSpec(getAESInitializingVector());
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

    @Override
    public void reset() throws IOException {
        try {
            init();
        } catch (GeneralSecurityException e) {
            throw new IOException("Security exception during reset", e);
        }
        super.reset();
        this.getInputStream().skip(16);
        this.haveReadStream = this.decryptingCOSStream;
    }
}
