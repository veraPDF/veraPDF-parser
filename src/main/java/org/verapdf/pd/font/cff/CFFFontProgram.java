package org.verapdf.pd.font.cff;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.pd.font.FontProgram;

import java.io.IOException;

/**
 * This class starts parsing for all inner CFF fonts and contains fonts parsed.
 *
 * @author Sergey Shemyakov
 */
public class CFFFontProgram extends CFFFileBaseParser implements FontProgram {

    private FontProgram font;
    private boolean isCIDFont = false;
    private boolean isFontParsed = false;

    /**
     * Constructor from stream.
     *
     * @param stream is stream with CFF program.
     * @throws IOException if creation of @{link InternalInputStream} fails.
     */
    public CFFFontProgram(ASInputStream stream) throws IOException {
        super(stream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseFont() throws IOException {
        if (!isFontParsed) {
            isFontParsed = true;
            this.readHeader();
            this.readIndex();   // name
            long topOffset = this.source.getOffset();
            CFFIndex top = this.readIndex();
            this.definedNames = this.readIndex();
            if (isCIDFont(top.get(0))) {
                font = new CFFCIDFontProgram(this.source,
                        topOffset + top.getOffset(0) - 1 + top.getOffsetShift(),
                        topOffset + top.getOffset(1) - 1 + top.getOffsetShift());
                font.parseFont();
            } else {
                font = new CFFType1FontProgram(this.source,
                        this.definedNames,
                        topOffset + top.getOffset(0) - 1 + top.getOffsetShift(),
                        topOffset + top.getOffset(1) - 1 + top.getOffsetShift());
                font.parseFont();
            }
        }
    }

    private boolean isCIDFont(byte[] topDict) {
        try {
            byte rosOffset;
            int supplementFirstByte = topDict[4] & 0xFF;    // checking if first operator is really ROS
            if (supplementFirstByte < 247 && supplementFirstByte > 31) {
                rosOffset = 5;
            } else if (supplementFirstByte > 246 && supplementFirstByte < 255) {
                rosOffset = 6;
            } else if (supplementFirstByte == 28) {
                rosOffset = 7;
            } else if (supplementFirstByte == 29) {
                rosOffset = 9;
            } else {
                return false;
            }
            if (topDict[rosOffset] == 12 && topDict[rosOffset + 1] == 30) {
                isCIDFont = true;
                return true;
            }
            return false;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int code) {
        return this.font.getWidth(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String glyphName) {
        return this.font.getWidth(glyphName);
    }

    /**
     * @return true if this font is CFF CID font.
     */
    public boolean isCIDFont() {
        return isCIDFont;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCID(int cid) {
        return font.containsCID(cid);
    }

    /**
     * @return CID font or Type1 font that is presented by CFF program.
     */
    public FontProgram getFont() {
        return this.font;
    }
}
