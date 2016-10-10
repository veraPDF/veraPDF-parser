package org.verapdf.tools;

/**
 * Implements RC4 encryption algorithm.
 *
 * @author Sergey Shemyakov
 */
public class RC4Encryption {

    private int[] s = new int[256];
    private byte[] key;
    int i;
    int j;

    /**
     * Constructor from encryption key.
     *
     * @param key is encryption key.
     */
    public RC4Encryption(byte[] key) {
        this.key = key;
        keySchedulingAlgorithm();
        this.i = 0;
        this.j = 0;
    }

    /**
     * Encrypts passed data with use of current inner state of encryptor.
     *
     * @param data is unencrypted data.
     * @return encrypted data.
     */
    public byte[] encrypt(byte[] data) {
        byte[] res = new byte[data.length];
        for (int k = 0; k < data.length; k++) {
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
}
