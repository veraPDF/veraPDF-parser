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
     * @return a code specifying the algorithm to be used in encrypting and
     * decrypting the document.
     */
    public int getV() {
        Long v = getIntegerKey(ASAtom.V);
        if (v != null) {
            return v.intValue();
        }
        return DEFAULT_V;
    }

    /**
     * @return the length of the encryption key, in bits.
     */
    public int getLength() {
        Long length = getIntegerKey(ASAtom.LENGTH);
        if (length != null) {
            return length.intValue();
        }
        return DEFAULT_LENGTH;
    }

    // Methods for standard encryption dictionary

    /**
     * @return a number specifying revision of the standard security handler.
     */
    public Long getR() {
        return getIntegerKey(ASAtom.R);
    }

    public COSString getO() {
        COSObject o = getKey(ASAtom.O);
        if (o != null && o.getType() == COSObjType.COS_STRING) {
            return (COSString) o.getDirectBase();
        }
        return null;
    }

    public COSString getU() {
        COSObject o = getKey(ASAtom.U);
        if (o != null && o.getType() == COSObjType.COS_STRING) {
            return (COSString) o.getDirectBase();
        }
        return null;
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
}
