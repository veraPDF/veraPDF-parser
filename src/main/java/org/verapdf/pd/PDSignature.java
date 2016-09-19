package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSString;

/**
 * Represents digital signature on pd level.
 *
 * @author Sergey Shemyakov
 */
public class PDSignature extends PDObject {

    public PDSignature(COSDictionary dict) {
        super(new COSObject(dict));
    }

    /**
     * @return int[] representation of ByteRange entry.
     */
    public int[] getByteRange() {
        COSObject cosByteRange = this.getKey(ASAtom.BYTERANGE);
        if (cosByteRange != null) {
            COSArray array = (COSArray) cosByteRange.get();
            if (array.size() >= 4) {
                int[] res = new int[4];
                for (int i = 0; i < 4; ++i) {
                    res[i] = array.at(i).getInteger().intValue();
                }
                return res;
            }
        }
        return null;
    }

    /**
     * @return array of signature reference dictionaries.
     */
    public COSArray getReference() {
        return (COSArray) this.getKey(ASAtom.REFERENCE).get();
    }

    /**
     * @return COSString that contains Contents of PDSignature.
     */
    public COSString getContents() {
        COSString contents = (COSString) this.getKey(ASAtom.CONTENTS).get();
        return contents;
    }
}
