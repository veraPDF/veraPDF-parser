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

/**
 * Implements RC4 encryption algorithm.
 *
 * @author Sergey Shemyakov
 */
public class RC4Encryption {

    private int[] s = new int[256];
    private int[] key;
    private int i;
    private int j;

    /**
     * Constructor from encryption key.
     *
     * @param key is encryption key.
     */
    public RC4Encryption(byte[] key) {
        this.key = new int[key.length];
        arraysCopy(key, this.key);
        reset();
    }

    /**
     * Encrypts or decrypts passed data with use of current inner state of
     * encryptor.
     *
     * @param data is data to process.
     * @return processed data.
     */
    public byte[] process(byte[] data) {
        return process(data, 0, data.length);
    }

    /**
     * Encrypts or decrypts passed data with use of current inner state of
     * encryptor.
     *
     * @param data   is data to process.
     * @param offset is offset of beginning of data to process.
     * @param size   is amount of bytes to process.
     * @return processed data.
     */
    public byte[] process(byte[] data, int offset, int size) {
        if (size >= 0 && data.length >= offset) {
            int actualSize = Math.min(size, data.length - offset);
            byte[] res = new byte[actualSize];
            for (int k = offset; k < actualSize; k++) {
                res[k] = (byte) (data[k] ^ getNextPseudoRandomByte());
            }
            return res;
        } else {
            return new byte[]{};
        }
    }

    /**
     * Resets inner state of encryptor to default.
     */
    public void reset() {
        this.i = 0;
        this.j = 0;
        keySchedulingAlgorithm();
    }

    private void keySchedulingAlgorithm() {
        for (int i = 0; i < 256; i++) {
            s[i] = i;
        }
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + s[i] + key[i % key.length]) % 256;
            swapSElements(i, j);
        }
    }

    private byte getNextPseudoRandomByte() {
        i = (i + 1) % 256;
        j = (j + s[i]) % 256;
        swapSElements(i, j);
        return (byte) s[(s[i] + s[j]) % 256];
    }

    private void swapSElements(int i, int j) {
        int tmp;
        tmp = s[i];
        s[i] = s[j];
        s[j] = tmp;
    }

    private static void arraysCopy(byte[] src, int[] dst) {
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i] & 0xFF;
        }
    }
}
