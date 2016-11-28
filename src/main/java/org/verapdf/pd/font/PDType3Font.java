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
        this.setSuccessfullyParsed(true);
    }

    public COSDictionary getCharProcDict() {
        return (COSDictionary) this.dictionary.getKey(ASAtom.CHAR_PROCS).getDirectBase();
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
            return new PDResources(COSDictionary.construct());
        }
    }

    @Override
    public String getName() {
        return this.dictionary.getStringKey(ASAtom.NAME);
    }

    public boolean containsCharString(int code) {
        String glyphName = this.getEncodingMapping().getName(code);
        COSDictionary charProcs = this.getCharProcs();
        if(charProcs != null) {
            ASAtom asAtomGlyph = ASAtom.getASAtom(glyphName);
            return charProcs.knownKey(asAtomGlyph);
        }
        return false;
    }

    private COSDictionary getCharProcs() {
        return (COSDictionary) this.dictionary.getKey(ASAtom.CHAR_PROCS).getDirectBase();
    }
}
