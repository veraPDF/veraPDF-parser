package org.verapdf.pd.font.cff;

import org.verapdf.io.InternalInputStream;
import org.verapdf.pd.font.CFFNumber;
import org.verapdf.pd.font.PDFLibFontProgram;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Instance of this class represent a Type1 font from FontSet of
 * CFF file.
 *
 * @author Sergey Shemyakov
 */
class CFFType1FontProgram extends CFFFontBaseParser implements PDFLibFontProgram {

    //Top DICT
    private long privateDictOffset;
    private long privateDictSize;

    private long encodingOffset;
    private int[] encoding;     // array with mapping code -> gid
    private Map<String, Integer> charSet;   // mappings glyphName -> gid

    CFFType1FontProgram(InternalInputStream stream, CFFIndex definedNames,
                        long topDictBeginOffset, long topDictEndOffset) {
        super(stream);
        encodingOffset = 0;
        encoding = new int[256];
        this.definedNames = definedNames;
        this.topDictBeginOffset = topDictBeginOffset;
        this.topDictEndOffset = topDictEndOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseFont() throws IOException {
        this.source.seek(topDictBeginOffset);
        while (this.source.getOffset() < topDictEndOffset) {
            readTopDictUnit();
        }
        this.stack.clear();

        this.source.seek(this.privateDictOffset);
        while (this.source.getOffset() < this.privateDictOffset + this.privateDictSize) {
            this.readPrivateDictUnit();
        }

        this.source.seek(charStringsOffset);
        this.readCharStrings();

        this.source.seek(encodingOffset);
        this.readEncoding();

        this.source.seek(charSetOffset);
        this.readCharSet();

        this.readWidths();
    }

    @Override
    protected void readTopDictOneByteOps(int lastRead) {
        switch (lastRead) {
            case 16:    // encoding
                this.encodingOffset = stack.get(stack.size() - 1).getInteger();
                this.stack.clear();
                break;
            case 18:    // Private
                this.privateDictSize =
                        this.stack.get(stack.size() - 2).getInteger();
                this.privateDictOffset =
                        this.stack.get(stack.size() - 1).getInteger();
                this.stack.clear();
                break;
            default:
                this.stack.clear();
        }
    }

    private void readEncoding() throws IOException {
        if (encodingOffset == 0) {
            this.encoding = CFFPredefined.STANDARD_ENCODING;
        } else if (encodingOffset == 1) {
            this.encoding = CFFPredefined.EXPERT_ENCODING;
        } else {
            int format = this.readCard8() & 0xFF;
            int amount;
            switch (format) {
                case 0:
                case 128:
                    amount = this.readCard8() & 0xFF;
                    for (int i = 0; i < amount; ++i) {
                        this.encoding[this.readCard8()] = i;
                    }
                    if (format == 0) {
                        break;
                    }
                    this.readSupplements();
                    break;
                case 1:
                case 129:
                    amount = this.readCard8() & 0xFF;
                    int encodingPointer = 0;
                    for (int i = 0; i < amount; ++i) {
                        int first = this.readCard8() & 0xFF;
                        int nLeft = this.readCard8() & 0xFF;
                        for (int j = 0; j <= nLeft; ++j) {
                            encoding[(first + j)] = encodingPointer++;
                        }
                    }
                    if (format == 1) {
                        break;
                    }
                    this.readSupplements();
                    break;
                default:
                    break;
            }
        }
    }

    private void readSupplements() throws IOException {
        int nSups = this.readCard8() & 0xFF;
        for (int i = 0; i < nSups; ++i) {
            int code = this.readCard8() & 0xFF;
            int glyph = this.readCard16();
            encoding[code] = glyph;
        }
    }

    private void readCharSet() throws IOException {
        this.charSet = new HashMap<>();
        this.charSet.put(this.getStringBySID(0), 0);
        if (this.charSetOffset == 0) {
            initializeCharSet(CFFPredefined.ISO_ADOBE_CHARSET);
        } else if (this.charSetOffset == 1) {
            initializeCharSet(CFFPredefined.EXPERT_CHARSET);
        } else if (this.charSetOffset == 2) {
            initializeCharSet(CFFPredefined.EXPERT_SUBSET_CHARSET);
        } else {
            int format = this.readCard8();
            switch (format) {
                case 0:
                    for (int i = 1; i < nGlyphs; ++i) {
                        this.charSet.put(this.getStringBySID(this.readCard16()), i);
                    }
                    break;
                case 1:
                case 2:
                    try {
                        int charSetPointer = 1;
                        while (charSetPointer < nGlyphs) {
                            int first = this.readCard16();
                            int nLeft;
                            if (format == 1) {
                                nLeft = this.readCard8() & 0xFF;
                            } else {
                                nLeft = this.readCard16();
                            }
                            for (int i = 0; i <= nLeft; ++i) {
                                this.charSet.put(this.getStringBySID(first + i),
                                        charSetPointer++);
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IOException("Error in parsing ranges of CharString in CFF file", e);
                    }
                    break;
                default:
                    throw new IOException("Can't process format of CharSet in CFF file");
            }
        }
    }

    private void readWidths() throws IOException {
        for (int i = 0; i < nGlyphs; ++i) {
            CFFNumber width = getWidthFromCharString(this.charStrings.get(i));
            float res = width.isInteger() ? width.getInteger() :
                    width.getReal();
            if (res == -1.) {
                res = this.defaultWidthX;
            } else {
                res += this.nominalWidthX;
            }
            this.widths[i] = res;
        }
        if (!Arrays.equals(this.fontMatrix, CFFType1FontProgram.DEFAULT_FONT_MATRIX)) {
            for (int i = 0; i < widths.length; ++i) {
                widths[i] = widths[i] * fontMatrix[0] * 1000;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int charCode) {
        try {
            return this.widths[this.encoding[charCode]];
        } catch (ArrayIndexOutOfBoundsException e) {
            return this.widths[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String charName) {
        Integer index = this.charSet.get(charName);
        if (index == null || index >= this.widths.length || index < 0) {
            return this.widths[0];
        }
        return this.widths[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCID(int cid) {
        return cid < nGlyphs;
    }

    private void initializeCharSet(String[] charSetArray) {
        for (int i = 0; i < charSetArray.length; ++i) {
            charSet.put(charSetArray[i], i);
        }
    }
}
