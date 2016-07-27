package org.verapdf.font.cff;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Base class for font from FontSet of CFF file.
 *
 * @author Sergey Shemyakov
 */
public abstract class CFFInnerFontParser extends CFFFileBaseParser {

    protected static final double[] DEFAULT_FONT_MATRIX = {0.001, 0, 0, 0.001, 0, 0};


    protected double[] fontMatrix = DEFAULT_FONT_MATRIX;
    protected int charSetOffset;
    protected int charStringsOffset;
    protected int privateDictOffset;
    protected int privateDictSize;
    protected int defaultWidthX;
    protected int nominalWidthX;
    ArrayList<CFFNumber> stack;

    protected CFFInnerFontParser(ASInputStream stream) throws IOException {
        super(stream);
        stack = new ArrayList<>(48);
    }

    protected void parseTopDict() throws IOException {
        try {
            int next = this.source.peek() & 0xFF;
            if ((next > 27 && next < 31) || (next > 31 && next < 255)) {
                this.stack.add(readNumber());
            } else {
                this.source.read();
                if (next > -1 && next < 22) {
                    switch (next) {
                        case 15:    // charset
                            this.charSetOffset = this.stack.get(0).getInteger();
                            this.stack.clear();
                            break;
                        case 17:    // CharStrings
                            this.charStringsOffset = this.stack.get(0).getInteger();
                            this.stack.clear();
                            break;
                        case 18:    // Private
                            this.privateDictSize = this.stack.get(0).getInteger();
                            this.privateDictOffset = this.stack.get(1).getInteger();
                            this.stack.clear();
                            break;
                        case 12:
                            next = this.source.read() & 0xFF;
                            switch (next) {
                                case 7:     // FontMatrix
                                    for (int i = 0; i < 6; ++i) {
                                        fontMatrix[i] = this.stack.get(i).getReal();
                                    }
                                    this.stack.clear();
                                    break;
                                default:
                                    parseTopDictTwoByteOps(next, this.stack);
                            }
                            break;
                        default:
                            parseTopDictOneByteOps(next, this.stack);
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Error with stack in processing Top DICT in CFF file", e);
        }
    }

    /**
     * Method helps to enlarge switch option while parsing operators in Top DICT.
     * It allows to add to consideration new one-byte operators in inherited
     * classes.
     *
     * @param lastRead is last read one-byte operator (i. e. >= 0 && <= 21) that
     *                 is used in switch.
     * @param stack    is ArrayList representing stack.
     */
    protected void parseTopDictOneByteOps(int lastRead, ArrayList<CFFNumber> stack) {
        stack.clear();
    }

    /**
     * Method helps to enlarge switch option while parsing operators in Top DICT.
     * It allows to add to consideration new two-byte operators in inherited
     * classes.
     *
     * @param lastRead is last read second byte of operator that is
     *                 used in switch.
     * @param stack    is ArrayList representing stack.
     */
    protected void parseTopDictTwoByteOps(int lastRead, ArrayList<CFFNumber> stack) {
        stack.clear();
    }

    protected void parsePrivateDict() throws IOException {
        int next = this.source.peek() & 0xFF;
        if ((next > 27 && next < 31) || (next > 31 && next < 255)) {
            this.stack.add(readNumber());
        } else {
            this.source.read();
            if (next > -1 && next < 22) {
                switch (next) {
                    case 20:    // defaultWidthX
                        this.defaultWidthX = this.stack.get(0).getInteger();
                        this.stack.clear();
                        break;
                    case 21:    // nominalWidthX
                        this.nominalWidthX = this.stack.get(0).getInteger();
                        this.stack.clear();
                        break;
                    default:
                        this.stack.clear(); // It's easy to add more options
                }
            }
        }
    }

}
