package org.verapdf.font.cff;

import org.verapdf.font.GeneralNumber;
import org.verapdf.font.cff.predefined.*;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Instance of this class represent a parser of Type1-like font from FontSet of
 * CFF file.
 *
 * @author Sergey Shemyakov
 */
public class CFFType1SubfontParser extends CFFInnerFontParser {

    private long encodingOffset;
    private int[] encoding;     // array with mapping code -> gid
    private String[] charSet;   // array with mappings gid -> glyphName

    public CFFType1SubfontParser(InternalInputStream stream, CFFIndex definedNames,
                                 long topDictBeginOffset, long topDictEndOffset)
            throws IOException {
        super(stream);
        encodingOffset = 0; // default
        encoding = new int[256];
        this.definedNames = definedNames;
        this.source.seek(topDictBeginOffset);
        while (this.source.getOffset() < topDictEndOffset) {
            readTopDictUnit();
        }
        this.clearStack();
    }

    @Override
    protected void readTopDictOneByteOps(int lastRead, ArrayList<GeneralNumber> stack) {
        switch (lastRead) {
            case 16:    // encoding
                this.encodingOffset = stack.get(stack.size() - 1).getInteger();
                stack.clear();
                break;
            default:
                stack.clear();
        }
    }

    @Override
    public void parseFont() throws IOException {
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

    protected void readEncoding() throws IOException {  // TODO: when we will have array of read fonts probably add logic of checking previously processed encodings and reducing
        if (encodingOffset == 0) {                      // problem to copying arrays.
            this.encoding = CFFStandardEncoding.STANDARD_ENCODING;  // TODO: do we need to do deep copy?
        } else if (encodingOffset == 1) {
            this.encoding = CFFExpertEncoding.EXPERT_ENCODING;
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
        this.charSet = new String[this.nGlyphs];
        this.charSet[0] = this.getStringBySID(0);
        if (this.charSetOffset == 0) {
            this.charSet = CFFISOAdobeCharset.ISO_ADOBE;    // TODO: do we need to do deep copy?
        } else if (this.charSetOffset == 1) {
            this.charSet = CFFExpertCharset.EXPERT;
        } else if (this.charSetOffset == 2) {
            this.charSet = CFFExpertSubsetCharset.EXPERT_SUBSET;
        } else {
            int format = this.readCard8();
            switch (format) {
                case 0:
                    for (int i = 1; i < nGlyphs; ++i) {
                        this.charSet[i] = this.getStringBySID(this.readCard16());
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
                                this.charSet[charSetPointer++] =
                                        this.getStringBySID(first + i);
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
            GeneralNumber width = getWidthFromCharString(this.charStrings.get(i));
            float res = width.isInteger() ? width.getInteger() :
                    width.getReal();
            if (res == -1.) {
                res = this.defaultWidthX;
            } else {
                res += this.nominalWidthX;
            }
            this.widths[i] = res;
        }
        if(!Arrays.equals(this.fontMatrix, this.DEFAULT_FONT_MATRIX)) {
            for (int i = 0; i < widths.length; ++i) {
                widths[i] = widths[i] * fontMatrix[0] * 1000;
            }
        }
    }
}
