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
public class EncryptionTools {

    private static final int PADDED_PASSWORD_LENGTH = 32;
    private static final int AMOUNT_OF_REPEATS = 50;
    private static final int[] DEFAULT_PADDING_STRING = new int[]{
            0x28, 0xBF, 0x4E, 0x5E, 0x4E, 0x75, 0x8A, 0x41,
            0x64, 0x00, 0x4E, 0x56, 0xFF, 0xFA, 0x01, 0x08,
            0x2E, 0x2E, 0x00, 0xB6, 0xD0, 0x68, 0x3E, 0x80,
            0x2F, 0x0C, 0xA9, 0xFE, 0x64, 0x53, 0x69, 0x7A
    };
    private static final byte[] FF_STRING = new byte[] {-1, -1, -1, -1};

    public static byte[] computeEncryptionKey(String password, byte[] o, int p,
                                              byte[] id, int revision,
                                              boolean metadataIsEncrypted,
                                              int length) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(getPaddedPassword(password));
        md5.update(o);
        md5.update(pToBytes(p));
        md5.update(id);
        if (revision >= 4  && !metadataIsEncrypted) {
            md5.update(FF_STRING);
        }
        byte [] res = md5.digest();
        if(revision >= 3) {
            for(int i = 0; i < AMOUNT_OF_REPEATS; ++i) {
                md5.reset();
                md5.update(Arrays.copyOf(res, length));
                res = md5.digest();
            }
        }
        return Arrays.copyOf()
    }

    private static byte[] getPaddedPassword(String password) {
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
                res[i + psw.length] = (byte) DEFAULT_PADDING_STRING[i];
            }
        }
        return res;
    }

    private static byte[] pToBytes(long p) {
        byte[] res = new byte[4];
        for (int i = 0; i < 4; ++i) {
            byte b = (byte) (p & 0xFF);
            p >>= 8;
            res[i] = b;
        }
        return res;
    }
}
