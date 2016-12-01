package org.verapdf.pd.font;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;
import org.verapdf.pd.font.truetype.CIDFontType2Program;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sergey Shemyakov
 */
public class PDCIDFont extends PDFont {

    private static final Logger LOGGER = Logger.getLogger(PDCIDFont.class.getCanonicalName());
    private static final Double DEFAULT_CID_FONT_WIDTH = Double.valueOf(1000d);

    protected CMap cMap;
    private CIDWArray widths;
    private PDCIDSystemInfo cidSystemInfo;

    public PDCIDFont(COSDictionary dictionary, CMap cMap) {
        super(dictionary);
        this.cMap = cMap;
    }

    /*
    Do not forget to set cMap!!!
     */
    protected PDCIDFont(COSDictionary dictionary) {
        super(dictionary);
    }

    public COSStream getCIDSet() {
        COSObject cidSet = this.fontDescriptor.getKey(ASAtom.CID_SET);
        return cidSet == null ? null : (COSStream) cidSet.getDirectBase();
    }

    public COSObject getCIDToGIDMap() {
        return this.dictionary.getKey(ASAtom.CID_TO_GID_MAP);
    }

    @Override
    public Double getWidth(int code) {
        if (this.widths == null) {
            COSObject w = this.dictionary.getKey(ASAtom.W);
            if (w.empty() || w.getType() != COSObjType.COS_ARRAY) {
                return Double.valueOf(0);
            }
            this.widths = new CIDWArray((COSArray) w.getDirectBase());
        }
        Double res = widths.getWidth(this.cMap.toCID(code));
        if (res == null) {
            COSObject dw = this.dictionary.getKey(ASAtom.DW);
            if (!dw.empty()) {
                res = dw.getReal();
            } else {
                res = DEFAULT_CID_FONT_WIDTH;
            }
        }
        return res;
    }

    @Override
    public Double getDefaultWidth() {
        COSObject dw = this.dictionary.getKey(ASAtom.DW);
        if (dw.getType().isNumber()) {
            return dw.getReal();
        } else {
            return DEFAULT_CID_FONT_WIDTH;
        }
    }

    @Override
    public int readCode(InputStream stream) throws IOException {
        if (cMap != null) {
            return cMap.getCodeFromStream(stream);
        }
        throw new IOException("No CMap for Type 0 font " +
                (this.getName() == null ? "" : this.getName()));
    }

    @Override
    public FontProgram getFontProgram() {
        if (this.isFontParsed) {
            return this.fontProgram;
        }
        this.isFontParsed = true;

        if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE2) &&
                this.getSubtype() == ASAtom.CID_FONT_TYPE2) {
            try {
                COSStream trueTypeFontFile = fontDescriptor.getFontFile2();
                this.fontProgram = new CIDFontType2Program(
                        trueTypeFontFile.getData(COSStream.FilterFlags.DECODE),
                        this.cMap, this.getCIDToGIDMap());
                return this.fontProgram;
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't read TrueType font program.", e);
            }
        } else if (fontDescriptor.canParseFontFile(ASAtom.FONT_FILE3)) {
            try {
                COSStream fontFile = fontDescriptor.getFontFile3();
                COSName subtype = (COSName) fontFile.getKey(ASAtom.SUBTYPE).getDirectBase();
                if (ASAtom.CID_FONT_TYPE0C == subtype.getName()) {
                    this.fontProgram = new CFFFontProgram(
                            fontFile.getData(COSStream.FilterFlags.DECODE),
                            this.getEncodingMapping(), this.cMap, this.isSubset());
                    return this.fontProgram;
                } else if (ASAtom.OPEN_TYPE == subtype.getName()) {
                    ASAtom fontName = this.getFontName();
                    if (fontName == ASAtom.TRUE_TYPE || fontName == ASAtom.CID_FONT_TYPE2) {
                        this.fontProgram = new OpenTypeFontProgram(
                                fontFile.getData(COSStream.FilterFlags.DECODE),
                                false, this.isSymbolic(), this.getEncoding(),
                                this.cMap, this.isSubset());
                        return this.fontProgram;
                    }
					this.fontProgram = new OpenTypeFontProgram(
					        fontFile.getData(COSStream.FilterFlags.DECODE),
					        true, this.isSymbolic(), this.getEncoding(),
                            this.cMap, this.isSubset());
					return this.fontProgram;
                }
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't read CFF font program.", e);
            }
        }
        this.fontProgram = null;
        return null;
    }

    public PDCIDSystemInfo getCIDSystemInfo() {
        if (this.cidSystemInfo != null) {
            return this.cidSystemInfo;
        } else {
            this.cidSystemInfo =
                    new PDCIDSystemInfo(this.dictionary.getKey(ASAtom.CID_SYSTEM_INFO));
            return this.cidSystemInfo;
        }
    }
}
