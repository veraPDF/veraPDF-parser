package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResources;

/**
 * @author Sergey Shemyakov
 */
public class PDType3Font extends PDSimpleFont {

    public PDType3Font(COSDictionary dictionary) {
        super(dictionary);
    }

    public COSDictionary getCharProcDict() {
        return (COSDictionary) this.dictionary.getKey(ASAtom.CHAR_PROCS).get();
    }

    @Override
    public FontProgram getFontProgram() {
        return null;
    }

    public PDResources getResources() {
        COSObject resources = this.dictionary.getKey(ASAtom.RESOURCES);
        if (!resources.empty() && resources.getType() == COSObjType.COS_DICT) {
            if (resources.isIndirect()) {
                resources = resources.getDirect();
            }
            return new PDResources(resources);
        } else {
            return null;
        }
    }
}
