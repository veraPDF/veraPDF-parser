package org.verapdf.font.cff;

import org.verapdf.font.CFFNumber;
import org.verapdf.font.type1.Type1CharStringParser;
import org.verapdf.io.ASMemoryInStream;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This is base class for CFF CID font and CFF Type 1 font parsers.
 *
 * @author Sergey Shemyakov
 */
abstract class CFFFontBaseParser extends CFFFileBaseParser {

    protected static final float[] DEFAULT_FONT_MATRIX =
            {(float) 0.001, 0, 0, (float) 0.001, 0, 0};
    protected ArrayList<CFFNumber> stack;

    //Top DICT
    protected float[] fontMatrix = new float[6];
    protected long charStringsOffset;
    protected long charSetOffset;
    protected int charStringType;

    //CharStrings
    protected int nGlyphs;
    protected CFFIndex charStrings;
    protected float[] widths;

    //Private DICT
    protected int defaultWidthX;
    protected int nominalWidthX;

    public CFFFontBaseParser(InternalInputStream source) throws IOException {
        super(source);
        stack = new ArrayList<>(48);
        System.arraycopy(DEFAULT_FONT_MATRIX, 0, this.fontMatrix, 0,
                DEFAULT_FONT_MATRIX.length);
        this.charStringType = 2;
        this.charSetOffset = 0; // default
    }

    protected void readTopDictUnit() throws IOException {
        try {
            int next = this.source.peek() & 0xFF;
            if ((next > 27 && next < 31) || (next > 31 && next < 255)) {
                this.stack.add(readNumber());
            } else {
                this.source.readByte();
                if (next > -1 && next < 22) {
                    switch (next) {
                        case 15:    // charset
                            this.charSetOffset =
                                    this.stack.get(this.stack.size() - 1).getInteger();
                            this.stack.clear();
                            break;
                        case 17:    // CharStrings
                            this.charStringsOffset =
                                    this.stack.get(stack.size() - 1).getInteger();
                            this.stack.clear();
                            break;
                        case 12:
                            next = this.source.readByte() & 0xFF;
                            switch (next) {
                                case 7:     // FontMatrix
                                    for (int i = 0; i < 6; ++i) {
                                        fontMatrix[i] = this.stack.get(i).getReal();
                                    }
                                    this.stack.clear();
                                    break;
                                case 6:     // Charstring Type
                                    this.charStringType = (int)
                                            this.stack.get(this.stack.size() - 1).getInteger();
                                    this.stack.clear();
                                    break;
                                default:
                                    readTopDictTwoByteOps(next);
                            }
                            break;
                        default:
                            readTopDictOneByteOps(next);
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Error with stack in processing Top DICT in CFF file", e);
        }
    }

    protected void readTopDictTwoByteOps(int lastRead) {
        this.stack.clear();
    }

    protected void readTopDictOneByteOps(int lastRead) {
        this.stack.clear();
    }

    protected void readCharStrings() throws IOException {
        this.charStrings = this.readIndex();
        this.nGlyphs = this.charStrings.size();
        widths = new float[nGlyphs];
    }

    protected void readPrivateDictUnit() throws IOException {
        int next = this.source.peek() & 0xFF;
        if ((next > 27 && next < 31) || (next > 31 && next < 255)) {
            this.stack.add(readNumber());
        } else {
            this.source.readByte();
            if (next > -1 && next < 22) {
                switch (next) {
                    case 20:    // defaultWidthX
                        this.defaultWidthX = (int)
                                this.stack.get(stack.size() - 1).getInteger();
                        this.stack.clear();
                        break;
                    case 21:    // nominalWidthX
                        this.nominalWidthX = (int)
                                this.stack.get(stack.size() - 1).getInteger();
                        this.stack.clear();
                        break;
                    default:
                        this.stack.clear();
                }
            }
        }
    }

    protected CFFNumber getWidthFromCharString(byte[] charString) throws IOException {
        if (this.charStringType == 1) {
            Type1CharStringParser parser = new Type1CharStringParser(
                    new ASMemoryInStream(charString));
            return parser.getWidth();
        } else if (this.charStringType == 2) {
            Type2CharStringParser parser = new Type2CharStringParser(
                    new ASMemoryInStream(charString));
            return parser.getWidth();
        } else {
            throw new IOException("Can't process CharString of type " + this.charStringType);
        }
    }

    /**
     * @return amount of glyphs in the font.
     */
    public int getNGlyphs() {
        return this.nGlyphs;
    }
}
