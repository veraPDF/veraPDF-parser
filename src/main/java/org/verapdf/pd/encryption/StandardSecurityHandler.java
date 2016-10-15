package org.verapdf.pd.encryption;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.*;
import org.verapdf.cos.filters.COSFilterAESDecryptionDefault;
import org.verapdf.cos.filters.COSFilterRC4DecryptionDefault;
import org.verapdf.tools.EncryptionTools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

/**
 * Class represents standard security handler. It authenticates user password
 * and calculates encryption key for document.
 *
 * @author Sergey Shemyakov
 */
public class StandardSecurityHandler {

    private static final Logger LOGGER = Logger.getLogger(StandardSecurityHandler.class);

    private PDEncryption pdEncryption;
    private COSObject id;
    private Boolean isEmptyStringPassword;
    private byte[] encryptionKey;
    private boolean isRC4Decryption;

    /**
     * Constructor.
     *
     * @param pdEncryption is encryption object of this PDF document.
     * @param id           is ID object from document trailer.
     */
    public StandardSecurityHandler(PDEncryption pdEncryption, COSObject id) {
        if (pdEncryption != null) {
            this.pdEncryption = pdEncryption;
        } else {
            this.pdEncryption = new PDEncryption();
        }
        this.id = id;
        this.isRC4Decryption = isRC4Decryption();
    }

    /**
     * Checks if empty string is a user password to this PDF document and sets
     * encryption key if password is successfully checked.
     *
     * @return true if empty string is a password to this PDF document.
     */
    public Boolean authenticateEmptyPassword() {
        byte[] o = getO();
        Long p = pdEncryption.getP();
        byte[] id = getID();
        Long revision = pdEncryption.getR();
        boolean encMetadata = pdEncryption.isEncryptMetadata();
        int length = pdEncryption.getLength();
        byte[] u = getU();
        if (o != null && p != null && id != null && revision != null && u != null) {
            try {
                this.encryptionKey = EncryptionTools.authenticateUserPassword("",
                        o, p.intValue(), id, revision.intValue(), encMetadata,
                        length, u);
                this.isEmptyStringPassword =
                        Boolean.valueOf(this.encryptionKey != null);
                return this.isEmptyStringPassword;
            } catch (NoSuchAlgorithmException e) {
                LOGGER.debug("Caught NoSuchAlgorithmException while document decryption", e);
                this.isEmptyStringPassword = Boolean.valueOf(false);
                return this.isEmptyStringPassword;
            }
        }
        LOGGER.debug("Can't authenticate password in encrypted PDF, something is null.");
        this.isEmptyStringPassword = Boolean.valueOf(false);
        return this.isEmptyStringPassword;
    }

    /**
     * @return encryption key for this security handler. If empty string is not
     * a valid password returns null.
     */
    public byte[] getEncryptionKey() {
        if (this.isEmptyStringPassword == null) {
            authenticateEmptyPassword();
        }
        if (!this.isEmptyStringPassword) {
            return null;
        }
        return this.encryptionKey;
    }

    /**
     * @return true if empty string is a valid password for this PDF document.
     */
    public boolean isEmptyStringPassword() {
        if (this.isEmptyStringPassword == null) {
            authenticateEmptyPassword();
        }
        return this.isEmptyStringPassword;
    }

    /**
     * Decrypts string and writes result into string.
     *
     * @param string    is COSString to decrypt.
     * @param stringKey is COSKey of object that contains this COSString.
     */
    public void decryptString(COSString string, COSKey stringKey)
            throws IOException, GeneralSecurityException {
        byte[] stringBytes = getBytesOfHexString(string);
        ASInputStream stream = new ASMemoryInStream(stringBytes);
        ASInputStream filter;
        if (isRC4Decryption) {
            filter = new COSFilterRC4DecryptionDefault(stream, stringKey,
                    this.encryptionKey);
        } else {
            filter = new COSFilterAESDecryptionDefault(stream, stringKey,
                    this.encryptionKey);
        }
        byte[] buf = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
        byte[] res = new byte[0];
        int read = filter.read(buf, buf.length);
        while (read != -1) {
            res = ASBufferingInFilter.concatenate(res, res.length, buf, read);
            read = filter.read(buf, buf.length);
        }
        filter.close();
        string.set(new String(res, "ISO-8859-1"));
    }

    /**
     * Applies decryption filter to the stream so it can be read as unencrypted.
     *
     * @param stream is COSStream with encrypted data.
     * @param key    is COSKey of this stream.
     */
    public void decryptStream(COSStream stream, COSKey key)
            throws IOException, GeneralSecurityException {
        ASInputStream encStream = stream.getData();
        ASInputStream filter;
        if (isRC4Decryption) {
            filter = new COSFilterRC4DecryptionDefault(encStream, key,
                    this.encryptionKey);
        } else {
            filter = new COSFilterAESDecryptionDefault(encStream, key,
                    this.encryptionKey);
        }
        stream.setData(filter);
    }

    /**
     * @return PDEncryption of this security handler.
     */
    public PDEncryption getPdEncryption() {
        return pdEncryption;
    }

    private byte[] getO() {
        COSString o = pdEncryption.getO();
        if (o != null) {
            return getBytesOfHexString(o);
        }
        return null;
    }

    private byte[] getU() {
        COSString u = pdEncryption.getU();
        if (u != null) {
            return getBytesOfHexString(u);
        }
        return null;
    }

    private byte[] getID() {
        if (this.id != null && this.id.getType() == COSObjType.COS_ARRAY) {
            COSObject id1 = id.at(0);
            if (id1.getType() == COSObjType.COS_STRING) {
                return getBytesOfHexString((COSString) id1.getDirectBase());
            }
        }
        return null;
    }

    private static byte[] getBytesOfHexString(COSString s) {
        try {
            return s.get().getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return s.get().getBytes();
        }
    }

    private boolean isRC4Decryption() {
        if (this.pdEncryption.getV() >= 4) {
            PDCryptFilter stdCrypt = this.pdEncryption.getStandardCryptFilter();
            if (stdCrypt != null) {
                ASAtom method = stdCrypt.getMethod();
                return method != ASAtom.AESV3 && method != ASAtom.AESV2;
            }
        }
        return true;
    }
}
