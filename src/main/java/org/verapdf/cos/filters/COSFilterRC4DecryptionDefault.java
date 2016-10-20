package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSKey;
import org.verapdf.tools.EncryptionTools;
import org.verapdf.tools.RC4Encryption;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Filter that decrypts data using RC4 cipher decryption.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterRC4DecryptionDefault extends ASBufferingInFilter {

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

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if(this.bufferSize() == 0) {
            int bytesFed = (int) this.feedBuffer(getBufferCapacity());
            if (bytesFed == -1) {
                return -1;
            }
        }
        byte[] encData = new byte[BF_BUFFER_SIZE];
        int encDataLength = this.bufferPopArray(encData, size);
        byte[] res = rc4.process(encData, 0, encDataLength);
        System.arraycopy(res, 0, buffer, 0, encDataLength);
        return encDataLength;
    }

    @Override
    public int read(byte[] buffer, int off, int size) throws IOException {
        if(this.bufferSize() == 0) {
            int bytesFed = (int) this.feedBuffer(getBufferCapacity());
            if (bytesFed == -1) {
                return -1;
            }
        }
        byte[] encData = new byte[BF_BUFFER_SIZE];
        int encDataLength = this.bufferPopArray(encData, size - off);
        byte[] res = rc4.process(encData, 0, encDataLength);
        System.arraycopy(res, 0, buffer, off, encDataLength);
        return encDataLength;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        this.rc4.reset();
    }

    private void initRC4(COSKey objectKey, byte[] encryptionKey)
            throws NoSuchAlgorithmException {
        byte[] objectKeyDigest = getObjectKeyDigest(objectKey);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(ASBufferingInFilter.concatenate(encryptionKey,
                encryptionKey.length, objectKeyDigest, objectKeyDigest.length));
        byte[] resultEncryptionKey = md5.digest();
        int keyLength = Math.min(MAXIMAL_KEY_LENGTH, resultEncryptionKey.length);
        rc4 = new RC4Encryption(Arrays.copyOf(resultEncryptionKey, keyLength));
    }

    public static byte[] getObjectKeyDigest(COSKey objectKey) {
        byte[] res = new byte[5];
        System.arraycopy(EncryptionTools.intToBytesLowOrderFirst(
                objectKey.getNumber()), 0, res, 0, 3);
        System.arraycopy(EncryptionTools.intToBytesLowOrderFirst(
                objectKey.getGeneration()), 0, res, 3, 2);
        return res;
    }
}
