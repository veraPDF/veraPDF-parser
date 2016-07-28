package org.verapdf.font.cff;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.font.cff.predefined.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Instance of this class represent a parser of Type1-like font from FontSet of
 * CFF file.
 *
 * @author Sergey Shemyakov
 */
public class CFFType1SubfontParser extends CFFInnerFontParser {

    private int encodingOffset;
    private int[] encoding;     // array with mapping code -> gid
    private String[] charSet;   // array with mappings gid -> glyphName

    public CFFType1SubfontParser(ASInputStream stream) throws IOException {
        super(stream);
        encodingOffset = 0; // default
        encoding = new int[256];
    }

    @Override
    protected void parseTopDictOneByteOps(int lastRead, ArrayList<CFFNumber> stack) {
        switch (lastRead) {
            case 16:    // encoding
                this.encodingOffset = stack.get(0).getInteger();
                stack.clear();
                break;
            default:
                stack.clear();
        }
    }

    protected void readEncoding() throws IOException {  // TODO: when we will have array of read fonts probably add logic of checking previously processed encodings and reducing
        if (encodingOffset == 0) {                       // problem to copying arrays.
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
        switch (this.charSetOffset) {
            case 0:
                this.charSet = CFFISOAdobeCharset.ISO_ADOBE;    // TODO: do we need to do deep copy?
                break;
            case 1:
                this.charSet = CFFExpertCharset.EXPERT;
                break;
            case 2:
                this.charSet = CFFExpertSubsetCharset.EXPERT_SUBSET;
                break;
            default:    // user-defined charset
                byte format = this.readCard8();
                switch (format) {
                    case 0:
                        for (int i = 1; i < nGlyphs; ++i) {
                            this.charSet[i] = this.getStringBySID(this.readCard16());
                        }
                        break;
                    case 1:
                    case 2:
                        try {
                            int charSetPointer = 0;
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

    private void readWidths() {
        for(int i = 0; i < nGlyphs; ++i) {

        }
    }
}
