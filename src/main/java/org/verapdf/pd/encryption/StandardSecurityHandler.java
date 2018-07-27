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
package org.verapdf.pd.encryption;

import org.verapdf.as.ASAtom;
import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.*;
import org.verapdf.cos.filters.COSFilterAESDecryptionDefault;
import org.verapdf.cos.filters.COSFilterRC4DecryptionDefault;
import org.verapdf.tools.EncryptionToolsRevision4;
import org.verapdf.tools.EncryptionToolsRevision5_6;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class represents standard security handler. It authenticates user password
 * and calculates encryption key for document.
 *
 * @author Sergey Shemyakov
 */
public class StandardSecurityHandler {

    private static final Logger LOGGER = Logger.getLogger(StandardSecurityHandler.class.getCanonicalName());

    private PDEncryption pdEncryption;
    private COSObject id;
    private Boolean isEmptyStringPassword;
    private byte[] encryptionKey;
    private boolean isRC4Decryption;
    private ASAtom method;
    private COSDocument document;

    /**
     * Constructor.
     *
     * @param pdEncryption is encryption object of this PDF document.
     * @param id           is ID object from document trailer.
     * @param document     is COSDocument object associated with this security handler
     */
    public StandardSecurityHandler(PDEncryption pdEncryption, COSObject id, COSDocument document) {
        if (pdEncryption != null) {
            this.pdEncryption = pdEncryption;
        } else {
            this.pdEncryption = new PDEncryption();
        }
        this.id = id;
        this.document = document;
        PDCryptFilter stdCrypt = this.pdEncryption.getStandardCryptFilter();
        if (stdCrypt != null) {
            this.method = stdCrypt.getMethod();
        }
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
                if (revision <= 4) {
                    this.encryptionKey = EncryptionToolsRevision4.authenticateUserPassword("",
                            o, p.intValue(), id, revision.intValue(), encMetadata,
                            length, u);
                } else if (revision >= 5) {    //   Revision 5 should not be used
                    this.encryptionKey = EncryptionToolsRevision5_6.getFileEncryptionKey("".getBytes(), o, u,
                            getOE(), getUE(), revision);
                }
                this.isEmptyStringPassword =
                        Boolean.valueOf(this.encryptionKey != null);
                return this.isEmptyStringPassword;
            } catch (GeneralSecurityException e) {
                LOGGER.log(Level.FINE, "Caught Security Exception while document decryption", e);
                this.isEmptyStringPassword = Boolean.valueOf(false);
                return this.isEmptyStringPassword;
            }
        }
        LOGGER.log(Level.FINE, "Can't authenticate password in encrypted PDF, something is null.");
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
        if (!this.isEmptyStringPassword.booleanValue()) {
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
        return this.isEmptyStringPassword.booleanValue();
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
                    this.encryptionKey, false, method);
        }
        byte[] buf = new byte[ASBufferedInFilter.BF_BUFFER_SIZE];
        byte[] res = new byte[0];
        filter.reset();
        int read = filter.read(buf, buf.length);
        while (read != -1) {
            res = ASBufferedInFilter.concatenate(res, res.length, buf, read);
            read = filter.read(buf, buf.length);
        }
        filter.close();
        string.set(res);
    }

    /**
     * Applies decryption filter to the stream so it can be read as unencrypted.
     *
     * @param stream is COSStream with encrypted data.
     * @param key    is COSKey of this stream.
     */
    public void decryptStream(COSStream stream, COSKey key)
            throws IOException, GeneralSecurityException {
        if (decryptRequired(stream)) {
            ASInputStream encStream = stream.getData();
            ASInputStream filter;
            if (isRC4Decryption) {
                filter = new COSFilterRC4DecryptionDefault(encStream, key,
                        this.encryptionKey);
            } else {
                filter = new COSFilterAESDecryptionDefault(encStream, key,
                        this.encryptionKey, true, method);
            }
            document.addFileResource(new ASFileStreamCloser(filter));
            stream.setData(filter, COSStream.FilterFlags.RAW_DATA);
        }
    }

    private boolean decryptRequired(COSStream stream) {
        boolean res = true;
        List<ASAtom> filters = stream.getFilters().getFilters();
        for (int i = 0; i < filters.size(); ++i) {
            if (ASAtom.CRYPT == filters.get(i)) {
                COSObject paramsObj = stream.getKey(ASAtom.DECODE_PARMS);
                if (paramsObj.empty() || paramsObj.getType() == COSObjType.COS_NULL) {
                    res = false;
                    continue;
                } else if (paramsObj.getType().isDictionaryBased()
                        && (i != 0 || paramsObj.getNameKey(ASAtom.NAME) == ASAtom.IDENTITY)) {
                    res = false;
                    continue;
                } else if (paramsObj.getType() == COSObjType.COS_ARRAY) {
                    if (paramsObj.size() - 1 < i) {
                        res = false;
                        continue;
                    } else {
                        COSObject params = paramsObj.at(i);
                        if (params == null || params.empty() || params.getType() == COSObjType.COS_NULL ||
                                (params.getType().isDictionaryBased() && paramsObj.getNameKey(ASAtom.NAME) == ASAtom.IDENTITY)) {
                            res = false;
                            continue;
                        }
                    }
                }
                res = true;
            }
        }
        return res;
    }

    /**
     * @return PDEncryption of this security handler.
     */
    public PDEncryption getPdEncryption() {
        return pdEncryption;
    }

    private byte[] getO() {
        return getBytesOfHexString(pdEncryption.getO());
    }

    private byte[] getU() {
        return getBytesOfHexString(pdEncryption.getU());
    }

    private byte[] getOE() {
        return getBytesOfHexString(pdEncryption.getOE());
    }

    private byte[] getUE() {
        return getBytesOfHexString(pdEncryption.getUE());
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
        if (s == null) {
            return null;
        }
        return s.get();
    }

    private boolean isRC4Decryption() {
        if (this.pdEncryption.getV() >= 4) {
            return method != ASAtom.AESV3 && method != ASAtom.AESV2;
        }
        return true;
    }
}
