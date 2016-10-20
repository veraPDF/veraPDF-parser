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
        this.i = 0;
        this.j = 0;
        keySchedulingAlgorithm();
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
        int actualSize = Math.min(size, data.length - offset);
        byte[] res = new byte[actualSize];
        for (int k = offset; k < actualSize; k++) {
            res[k] = (byte) (data[k] ^ getNextPseudoRandomByte());
        }
        return res;
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
