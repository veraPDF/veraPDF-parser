package org.verapdf.font.cff;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.font.PDFlibFont;

import java.io.IOException;

/**
 * This class starts parsing for all inner CFF fonts and contains fonts parsed.
 *
 * @author Sergey Shemyakov
 */
public class CFFFont extends CFFFileBaseParser implements PDFlibFont {

    private PDFlibFont font;

    public CFFFont(ASInputStream stream) throws IOException {
        super(stream);
    }

    @Override
    public void parseFont() throws IOException {
        this.readHeader();
        this.readIndex();   // name
        long topOffset = this.source.getOffset();
        CFFIndex top = this.readIndex();
        this.definedNames = this.readIndex();
        if (isCIDFont(top.get(0))) {
            //TODO: CID fonts
        } else {
            font = new CFFType1Font(this.source,
                    this.definedNames,
                    topOffset + top.getOffset(0) - 1 + top.getOffsetShift(),
                    topOffset + top.getOffset(1) - 1 + top.getOffsetShift());
            font.parseFont();
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
            return topDict[rosOffset] == 12 && topDict[rosOffset + 1] == 30;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    @Override
    public float getWidth(int code) {
        return this.font.getWidth(code);
    }

    @Override
    public float getWidth(String glyphName) {
        return this.font.getWidth(glyphName);
    }
}
