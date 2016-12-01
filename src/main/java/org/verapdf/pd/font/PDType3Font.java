package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSBase;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDResources;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sergey Shemyakov
 */
public class PDType3Font extends PDSimpleFont {

    private static final Logger LOGGER = Logger.getLogger(PDType3Font.class.getCanonicalName());

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

    /**
     * @return a rectangle, expressed in the glyph coordinate system, that shall
     * specify the font bounding box.
     */
    public double[] getFontBoundingBox() {
        COSBase bbox = this.getObject().getKey(ASAtom.FONT_BBOX).get();
        if (bbox.getType() == COSObjType.COS_ARRAY || bbox.size() == 4) {
            double[] res = new double[4];
            for (int i = 0; i < 4; ++i) {
                COSObject obj = bbox.at(i);
                if (obj.getType().isNumber()) {
                    res[i] = obj.getReal();
                } else {
                    String fontName = getName() == null ? "" : getName();
                    LOGGER.log(Level.FINE, "Font bounding box array for font " + fontName +
                            " contains " + obj.getType());
                    return null;
                }
            }
            return res;
        } else {
            String fontName = getName() == null ? "" : getName();
            LOGGER.log(Level.FINE, "Font bounding box array for font " + fontName +
                    " is not an array of 4 elements");
            return null;
        }
    }

    public double[] getFontMatrix() {
        COSObject fontMatrix = this.dictionary.getKey(ASAtom.FONT_MATRIX);
        if (fontMatrix.getType() == COSObjType.COS_ARRAY && fontMatrix.size() == 6) {
            double[] res = new double[6];
            for (int i = 0; i < res.length; ++i) {
                 if (fontMatrix.at(i).getType().isNumber()) {
                     res[i] = fontMatrix.at(i).getReal();
                 } else {
                     return null;
                 }
            }
            return res;
        }
        return null;
    }

    private COSDictionary getCharProcs() {
        return (COSDictionary) this.dictionary.getKey(ASAtom.CHAR_PROCS).getDirectBase();
    }
}
