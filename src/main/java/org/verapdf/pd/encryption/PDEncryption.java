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
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSString;
import org.verapdf.pd.PDObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents encryption dictionary on PD level.
 *
 * @author Sergey Shemyakov
 */
public class PDEncryption extends PDObject {

    private static final boolean DEFAULT_ENCRYPT_METADATA = true;
    private static final int DEFAULT_LENGTH = 40;
    private static final int DEFAULT_V = 0;
    private Map<ASAtom, PDCryptFilter> cryptFilters;

    /**
     * Constructor from encryption dictionary.
     *
     * @param obj is encryption dictionary.
     */
    public PDEncryption(COSObject obj) {
        super(obj);
        this.cryptFilters = getCryptFilters();
    }

    public PDEncryption() {
        super();
    }

    /**
     * @return the name of the preferred security handler for this document.
     */
    public ASAtom getFilter() {
        return getNameKey(ASAtom.FILTER);
    }

    /**
     * @return a name that completely specifies the format and interpretation
     * of the contents of the encryption dictionary.
     */
    public ASAtom getSubFilter() {
        return getNameKey(ASAtom.SUB_FILTER);
    }

    public AccessPermissions getUserPermissions() {
        Long p = getP();
        return p == null ? null : AccessPermissions.getUserPermissions(p.intValue());
    }

    /**
     * @return a code specifying the algorithm to be used in encrypting and
     * decrypting the document.
     */
    public int getV() {
        return getIntWithDefault(ASAtom.V, DEFAULT_V);
    }

    /**
     * @return the length of the encryption key, in bits.
     */
    public int getLength() {
        return getIntWithDefault(ASAtom.LENGTH, DEFAULT_LENGTH);
    }

    // Methods for standard encryption dictionary

    /**
     * @return a number specifying revision of the standard security handler.
     */
    public Long getR() {
        return getIntegerKey(ASAtom.R);
    }

    public COSString getO() {
        return getCOSString(ASAtom.O);
    }

    public COSString getU() {
        return getCOSString(ASAtom.U);
    }

    public Long getP() {
        return getIntegerKey(ASAtom.P);
    }

    /**
     * @return true if document-level metadata stream shall be be encrypted.
     */
    public boolean isEncryptMetadata() {
        COSObject encryptMetadata = getKey(ASAtom.ENCRYPT_META_DATA);
        if (encryptMetadata != null && encryptMetadata.getType() == COSObjType.COS_BOOLEAN) {
            return encryptMetadata.getDirectBase().getBoolean();
        }
        return DEFAULT_ENCRYPT_METADATA;
    }

    /**
     * @return standard crypt filter for standard security handler of revision 4.
     */
    public PDCryptFilter getStandardCryptFilter() {
        return getCryptFilter(ASAtom.STD_CF);
    }

    /**
     * Gets crypt filter with given name.
     *
     * @param cfName is name for crypt filter.
     * @return crypt filter for this name taken from encryption dictionary CF
     * dict.
     */
    public PDCryptFilter getCryptFilter(ASAtom cfName) {
        return this.cryptFilters.get(cfName);
    }

    private Map<ASAtom, PDCryptFilter> getCryptFilters() {
        HashMap<ASAtom, PDCryptFilter> res = new HashMap<>();
        COSObject cf = getKey(ASAtom.CF);
        if (cf == null || cf.getType() != COSObjType.COS_DICT) {
            return res;
        }
        Set<ASAtom> filters = cf.getKeySet();
        for (ASAtom filterName : filters) {
            res.put(filterName, new PDCryptFilter(cf.getKey(filterName)));
        }
        return res;
    }

    private COSString getCOSString(ASAtom key) {
        COSObject o = getKey(key);
        if (o != null && o.getType() == COSObjType.COS_STRING) {
            return (COSString) o.getDirectBase();
        }
        return null;
    }

    private int getIntWithDefault(ASAtom key, int defaultValue) {
        Long res = getIntegerKey(key);
        if (res != null) {
            return res.intValue();
        }
        return defaultValue;
    }
}
