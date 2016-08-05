package org.verapdf.font.cff;

import org.verapdf.font.GeneralNumber;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Instance of this class represent a parser of CIDFont from FontSet of CFF file.
 *
 * @author Sergey Shemyakov
 */
public class CFFCidSubfontParser extends CFFInnerFontParser {

    private long fdArrayOffset;
    private long fdSelectOffset;
    private int[] charSet;      // array with mapping gid -> cid
    private int[] fdSelect;     // array with mapping gid -> font dict
    private int[] nominalWidths;
    private int[] defaultWidths;

    public CFFCidSubfontParser(InternalInputStream stream, long topDictBeginOffset,
                               long topDictEndOffset) throws IOException {
        super(stream);
        this.source.seek(topDictBeginOffset);
        while (this.source.getOffset() < topDictEndOffset) {
            readTopDictUnit();
        }
    }

    @Override
    public void parseFont() throws IOException {
        this.source.seek(charStringsOffset);
        this.readCharStrings();

        this.source.seek(charSetOffset);
        this.readCharSet();

        this.source.seek(fdSelectOffset);
        this.readFDSelect();

        this.source.seek(fdArrayOffset);
        this.readFontDicts();

        this.readWidths();
    }

    @Override
    protected void readTopDictTwoByteOps(int lastRead, ArrayList<GeneralNumber> stack) {
        switch (lastRead) {
            case 36:
                this.fdArrayOffset = stack.get(stack.size() - 1).getInteger();
                stack.clear();
                break;
            case 37:
                this.fdSelectOffset = stack.get(stack.size() - 1).getInteger();
                stack.clear();
                break;
            default:
                stack.clear();
        }
    }

    private void readCharSet() throws IOException {
        this.charSet = new int[this.nGlyphs];
        this.charSet[0] = 0;
        int format = this.readCard8();
        switch (format) {
            case 0:
                for (int i = 1; i < nGlyphs; ++i) {
                    this.charSet[i] = this.readCard16();
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
                            this.charSet[charSetPointer++] = first + i;
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

    private void readFDSelect() throws IOException {
        try {
            int format = this.readCard8();
            this.fdSelect = new int[nGlyphs];
            if (format == 0) {
                for (int i = 0; i < nGlyphs; ++i) {
                    this.fdSelect[i] = this.readCard8();
                }
            } else if (format == 3) {
                int numberOfRanges = this.readCard16();
                int first = this.readCard16();
                for (int i = 0; i < numberOfRanges; ++i) {
                    int fd = this.readCard8();
                    int afterLast = this.readCard16();
                    for (int j = first; j < afterLast; ++j) {
                        this.fdSelect[j] = fd;
                    }
                    first = afterLast;
                }
            } else {
                throw new IOException("Can't parse format of FDSelect in CFF file");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Can't parse FDSelect in CFF file", e);
        }
    }

    private void readFontDicts() throws IOException {
        CFFIndex fontDictIndex = this.readIndex();
        this.nominalWidths = new int[fontDictIndex.size()];
        this.defaultWidths = new int[fontDictIndex.size()];
        for (int i = 0; i < fontDictIndex.size(); ++i) {
            this.readPrivateDict(
                    fontDictIndex.getOffset(i), fontDictIndex.getOffset(i + 1));
            this.nominalWidths[i] = this.nominalWidthX;
            this.defaultWidths[i] = this.defaultWidthX;
        }
    }

    private void readPrivateDict(long from, long to) throws IOException {
        this.clearStack();
        long startingOffset = this.source.getOffset();
        this.source.seek(from);
        while (this.source.getOffset() < to) {
            this.readPrivateDictUnit();
        }
        this.source.seek(startingOffset);
    }

    private void readWidths() throws IOException {
        for (int i = 0; i < nGlyphs; ++i) {
            GeneralNumber width = getWidthFromCharString(this.charStrings.get(i));
            float res = width.isInteger() ? width.getInteger() :
                    width.getReal();
            if (res == -1.) {
                res = this.defaultWidths[this.fdSelect[i]];
            } else {
                res += this.nominalWidths[this.fdSelect[i]];
            }
            this.widths[i] = res;
        }
        if (!Arrays.equals(this.fontMatrix, this.DEFAULT_FONT_MATRIX)) {
            for (int i = 0; i < widths.length; ++i) {
                widths[i] = widths[i] * fontMatrix[0] * 1000;
            }
        }
    }
}
