package org.verapdf.font.cff;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.font.GeneralNumber;
import org.verapdf.io.InternalInputStream;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Base class for font from FontSet of CFF file.
 *
 * @author Sergey Shemyakov
 */
public abstract class CFFInnerFontParser extends CFFFileBaseParser {

    protected static final float[] DEFAULT_FONT_MATRIX =
            {(float) 0.001, 0, 0, (float) 0.001, 0, 0};
    private ArrayList<GeneralNumber> stack;

    //Top DICT
    protected float[] fontMatrix;
    protected long charSetOffset;
    protected long charStringsOffset;
    protected long privateDictOffset;
    protected long privateDictSize;
    protected int charStringType;

    //Private DICT
    protected int defaultWidthX;
    protected int nominalWidthX;

    //CharStrings
    protected int nGlyphs;
    protected CFFIndex charStrings;
    protected float[] widths;


    protected CFFInnerFontParser(ASInputStream stream) throws IOException {
        super(stream);
        stack = new ArrayList<>(48);
        this.charSetOffset = 0; // default
        this.charStringType = 2;
        System.arraycopy(DEFAULT_FONT_MATRIX, 0, this.fontMatrix, 0,
                DEFAULT_FONT_MATRIX.length);
    }

    protected CFFInnerFontParser(InternalInputStream stream) {
        super(stream);
        stack = new ArrayList<>(48);
        this.charSetOffset = 0; // default
        this.charStringType = 2;
        System.arraycopy(DEFAULT_FONT_MATRIX, 0, this.fontMatrix, 0,
                DEFAULT_FONT_MATRIX.length);
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
                            next = this.source.readByte() & 0xFF;
                            switch (next) {
                                case 7:     // FontMatrix
                                    for (int i = 0; i < 6; ++i) {
                                        fontMatrix[i] = this.stack.get(i).getReal();
                                    }
                                    this.stack.clear();
                                    break;
                                case 6:     // Charstring Type
                                    this.charStringType = (int) this.stack.get(0).getInteger();
                                    this.stack.clear();
                                    break;
                                default:
                                    readTopDictTwoByteOps(next, this.stack);
                            }
                            break;
                        default:
                            readTopDictOneByteOps(next, this.stack);
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
    protected void readTopDictOneByteOps(int lastRead, ArrayList<GeneralNumber> stack) {
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
    protected void readTopDictTwoByteOps(int lastRead, ArrayList<GeneralNumber> stack) {
        stack.clear();
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
                        this.defaultWidthX = (int) this.stack.get(0).getInteger();
                        this.stack.clear();
                        break;
                    case 21:    // nominalWidthX
                        this.nominalWidthX = (int) this.stack.get(0).getInteger();
                        this.stack.clear();
                        break;
                    default:
                        this.stack.clear(); // It's easy to add more options
                }
            }
        }
    }

    protected void readCharStrings() throws IOException {
        this.charStrings = this.readIndex();
        this.nGlyphs = this.charStrings.size();
        widths = new float[nGlyphs];
    }

    public float[] getWidths() {
            return widths;
    }

    /**
     * This method does parsing of subfont extracting encoding info and width
     * info. Note that Header and Name INDEX should be read at this moment.
     */
    public abstract void parseFont() throws IOException;

}
