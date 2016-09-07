package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;

/**
 * @author Sergey Shemyakov
 */
public class PDType3Font extends PDFont {

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
}
