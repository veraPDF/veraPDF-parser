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
import org.verapdf.tools.EncryptionToolsRevision4;
import org.verapdf.tools.RC4Encryption;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Filter that decrypts data using RC4 cipher decryption according to Algorithm
 * 1 of 7.6.2 of ISO 32000:2008.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterRC4DecryptionDefault extends ASBufferedInFilter {

    public static final int MAXIMAL_KEY_LENGTH = 16;
    private RC4Encryption rc4;

    /**
     * Constructor.
     *
     * @param stream is stream with encrypted data.
     * @param objectKey contains object and generation numbers from object
     *                  identifier for object that is being decrypted. If it is
     *                  direct object, objectKey is taken from indirect object
     *                  that contains it.
     * @param encryptionKey is encryption key that is calculated from user
     *                      password and encryption dictionary.
     */
    public COSFilterRC4DecryptionDefault(ASInputStream stream, COSKey objectKey,
                                         byte[] encryptionKey)
            throws IOException, NoSuchAlgorithmException {
        super(stream);
        initRC4(objectKey, encryptionKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] buffer, int size) throws IOException {
        return this.read(buffer, 0, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] buffer, int off, int size) throws IOException {
        if(this.bufferSize() == 0) {
            int bytesFed = (int) this.feedBuffer(getBufferCapacity());
            if (bytesFed == -1) {
                return -1;
            }
        }
        byte[] encData = new byte[BF_BUFFER_SIZE];
        int encDataLength = this.bufferPopArray(encData, size);
        if (encDataLength >= 0) {
            byte[] res = rc4.process(encData, 0, encDataLength);
            System.arraycopy(res, 0, buffer, off, encDataLength);
        }
        return encDataLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws IOException {
        this.rc4.reset();
        super.reset();
    }

    private void initRC4(COSKey objectKey, byte[] encryptionKey)
            throws NoSuchAlgorithmException {
        byte[] objectKeyDigest = getObjectKeyDigest(objectKey);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(ASBufferedInFilter.concatenate(encryptionKey,
                encryptionKey.length, objectKeyDigest, objectKeyDigest.length));
        byte[] resultEncryptionKey = md5.digest();
        int keyLength = Math.min(MAXIMAL_KEY_LENGTH, encryptionKey.length +
                objectKeyDigest.length);
        rc4 = new RC4Encryption(Arrays.copyOf(resultEncryptionKey, keyLength));
    }

    /**
     * Gets a byte string consisting of object number and object generation
     * concatenated.
     *
     * @param objectKey is key of object.
     * @return byte string consisting of object number and object generation
     * concatenated.
     */
    public static byte[] getObjectKeyDigest(COSKey objectKey) {
        byte[] res = new byte[5];
        System.arraycopy(EncryptionToolsRevision4.intToBytesLowOrderFirst(
                objectKey.getNumber()), 0, res, 0, 3);
        System.arraycopy(EncryptionToolsRevision4.intToBytesLowOrderFirst(
                objectKey.getGeneration()), 0, res, 3, 2);
        return res;
    }
}
