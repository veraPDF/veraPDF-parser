package org.verapdf.pd.font.type1;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.parser.COSParser;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.PDSimpleFont;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.opentype.OpenTypeFontProgram;
import org.verapdf.pd.font.truetype.TrueTypePredefined;

import java.io.IOException;
import java.util.*;

/**
 * @author Sergey Shemyakov
 */
public class PDType1Font extends PDSimpleFont {

    private static final Logger LOGGER = Logger.getLogger(PDType1Font.class);
    public static final ASAtom[] STANDARD_FONT_NAMES = {
            ASAtom.COURIER_BOLD,
            ASAtom.COURIER_BOLD_OBLIQUE,
            ASAtom.COURIER,
            ASAtom.COURIER_OBLIQUE,
            ASAtom.HELVETICA,
            ASAtom.HELVETICA_BOLD,
            ASAtom.HELVETICA_BOLD_OBLIQUE,
            ASAtom.HELVETICA_OBLIQUE,
            ASAtom.SYMBOL,
            ASAtom.TIMES_BOLD,
            ASAtom.TIMES_BOLD_ITALIC,
            ASAtom.TIMES_ITALIC,
            ASAtom.TIMES_ROMAN,
            ASAtom.ZAPF_DINGBATS};

    private Boolean isStandard = null;

    public PDType1Font(COSDictionary dictionary) {
        super(dictionary);
    }

    public Set<String> getDescriptorCharSet() {
        String descriptorCharSetString =
                this.fontDescriptor.getStringKey(ASAtom.CHAR_SET);
        if (descriptorCharSetString != null) {
            try {
                ASMemoryInStream stream =
                        new ASMemoryInStream(descriptorCharSetString.getBytes());
                Set<String> descriptorCharSet = new TreeSet<>();
                COSParser parser = new COSParser(stream);
                COSObject glyphName = parser.nextObject();
                while (!glyphName.empty()) {
                    if (glyphName.getType() == COSObjType.COS_NAME) {
                        descriptorCharSet.add(glyphName.getString());
                    }
                    glyphName = parser.nextObject();
                }
                return descriptorCharSet;
            } catch (IOException ex) {
                LOGGER.debug("Can't parse /CharSet entry in Type 1 font descriptor");
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();
    }

    @Override
    public FontProgram getFontProgram() {
        if (this.isFontParsed) {
            return this.fontProgram;
        }
        this.isFontParsed = true;
        try {
            if (fontDescriptor.knownKey(ASAtom.FONT_FILE)) {
                COSStream type1FontFile =
                        getStreamFromObject(fontDescriptor.getKey(ASAtom.FONT_FILE));
                this.fontProgram = new Type1FontProgram(
                        type1FontFile.getData(COSStream.FilterFlags.DECODE));
                return this.fontProgram;
            } else if (fontDescriptor.knownKey(ASAtom.FONT_FILE3)) {
                COSStream type1FontFile =
                        getStreamFromObject(fontDescriptor.getKey(ASAtom.FONT_FILE3));
                ASAtom subtype = type1FontFile.getNameKey(ASAtom.SUBTYPE);
                if (subtype == ASAtom.TYPE1C) {

                    this.fontProgram = new CFFFontProgram(type1FontFile.getData(
                            COSStream.FilterFlags.DECODE));
                    return this.fontProgram;
                } else if (subtype == ASAtom.OPEN_TYPE) {
                    this.fontProgram = new OpenTypeFontProgram(type1FontFile.getData(
                            COSStream.FilterFlags.DECODE), true, this.isSymbolic(),
                            this.getEncoding());
                    return this.fontProgram;
                }
            }
        } catch (IOException e) {
            LOGGER.debug("Can't read Type 1 font program.");
        }
        this.fontProgram = null;
        return null;
    }

    public Boolean isStandard() {
        if(this.isStandard == null) {
            if (!containsDiffs() && !isEmbedded() && isNameStandard()) {
                isStandard = Boolean.valueOf(true);
                return isStandard;
            } else {
                isStandard = Boolean.valueOf(false);
                return isStandard;
            }
        } else {
            return this.isStandard;
        }
    }

    private boolean containsDiffs() {
        if (this.dictionary.getKey(ASAtom.ENCODING).getType() ==
                COSObjType.COS_DICT) {
            Map<Integer, String> differences = this.getDifferences();
            if (differences != null && differences.size() != 0) {
                String[] baseEncoding = getBaseEncoding((COSDictionary)
                        this.dictionary.getKey(ASAtom.ENCODING).getDirectBase());
                if (baseEncoding.length == 0) {
                    return true;
                }
                for (Map.Entry<Integer, String> entry : differences.entrySet()) {
                    if (!entry.getValue().equals(baseEncoding[entry.getKey()])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String[] getBaseEncoding(COSDictionary encoding) {
        ASAtom baseEncoding = encoding.getNameKey(ASAtom.BASE_ENCODING);
        if (baseEncoding == null) {
            return new String[]{};
        }
        if (baseEncoding == ASAtom.MAC_ROMAN_ENCODING) {
            return Arrays.copyOf(TrueTypePredefined.MAC_ROMAN_ENCODING,
                    TrueTypePredefined.MAC_ROMAN_ENCODING.length);
        } else if (baseEncoding == ASAtom.MAC_EXPERT_ENCODING) {
            return Arrays.copyOf(TrueTypePredefined.MAC_EXPERT_ENCODING,
                    TrueTypePredefined.MAC_EXPERT_ENCODING.length);
        } else if (baseEncoding == ASAtom.WIN_ANSI_ENCODING) {
            return Arrays.copyOf(TrueTypePredefined.WIN_ANSI_ENCODING,
                    TrueTypePredefined.WIN_ANSI_ENCODING.length);
        } else {
            return new String[]{};
        }
    }

    private boolean isEmbedded() {
        return this.getFontProgram() == null;
    }

    private boolean isNameStandard() {
        ASAtom fontName = this.getDictionary().getNameKey(ASAtom.BASE_FONT);
        for (ASAtom standard : STANDARD_FONT_NAMES) {
            if (standard == fontName) {
                return true;
            }
        }
        return false;
    }
}
