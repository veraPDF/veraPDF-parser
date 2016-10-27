package org.verapdf.pd.font;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;
import org.verapdf.pd.font.truetype.CIDFontType2Program;

/**
 * @author Sergey Shemyakov
 */
public class PDCIDFont extends PDFont {

    private static final Logger LOGGER = Logger.getLogger(PDCIDFont.class.getCanonicalName());
    private static final Double DEFAULT_CID_FONT_WIDTH = Double.valueOf(1000d);

    protected CMap cMap;
    private CIDWArray widths;

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
        Double res = widths.getWidth(code);
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
        if (!dw.empty()) {
            return dw.getReal();
        } else {
            return DEFAULT_CID_FONT_WIDTH;
        }
    }

    @Override
    public int readCode(InputStream stream) throws IOException {
        if (cMap != null) {
            return cMap.getCIDFromStream(stream);
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

        if (fontDescriptor.knownKey(ASAtom.FONT_FILE2).booleanValue() &&
                this.getSubtype() == ASAtom.CID_FONT_TYPE2) {
            try {
                COSStream trueTypeFontFile =
                        getStreamFromObject(fontDescriptor.getKey(ASAtom.FONT_FILE2));
                this.fontProgram = new CIDFontType2Program(
                        trueTypeFontFile.getData(COSStream.FilterFlags.DECODE),
                        this.cMap, this.getCIDToGIDMap());
                return this.fontProgram;
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't read TrueType font program.", e);
            }
        } else if (fontDescriptor.knownKey(ASAtom.FONT_FILE3).booleanValue()) {
            try {
                COSStream fontFile =
                        getStreamFromObject(fontDescriptor.getKey(ASAtom.FONT_FILE3));
                COSName subtype = (COSName) fontFile.getKey(ASAtom.SUBTYPE).getDirectBase();
                if (ASAtom.CID_FONT_TYPE0C == subtype.get()) {
                    this.fontProgram = new CFFFontProgram(
                            fontFile.getData(COSStream.FilterFlags.DECODE),
                            this.getEncodingMapping(), this.cMap);
                    return this.fontProgram;
                } else if (ASAtom.OPEN_TYPE == subtype.get()) {
                    ASAtom fontName = this.getFontName();
                    if (fontName == ASAtom.TRUE_TYPE || fontName == ASAtom.CID_FONT_TYPE2) {
                        this.fontProgram = new OpenTypeFontProgram(
                                fontFile.getData(COSStream.FilterFlags.DECODE),
                                false, this.isSymbolic(), this.getEncoding(), this.cMap);
                        return this.fontProgram;
                    }
					this.fontProgram = new OpenTypeFontProgram(
					        fontFile.getData(COSStream.FilterFlags.DECODE),
					        true, this.isSymbolic(), this.getEncoding(), this.cMap);
					return this.fontProgram;
                }
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Can't read CFF font program.", e);
            }
        }
        this.fontProgram = null;
        return null;
    }
}
