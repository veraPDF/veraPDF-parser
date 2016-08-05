package org.verapdf.font.cff;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Instance of this class parses data common for all inner CFF fonts and
 * initializes parsers for each of them.
 *
 * @author Sergey Shemyakov
 */
public class CFFSubfontParserStarter extends CFFFileBaseParser {

    private List<String> fontNames;
    private List<CFFInnerFontParser> fontParsers;

    CFFSubfontParserStarter(ASInputStream stream) throws IOException {
        super(stream);
        this.fontNames = new ArrayList<>();
        this.fontParsers = new ArrayList<>();
    }

    public void parse() throws IOException {
        this.readHeader();
        CFFIndex name = this.readIndex();
        long topOffset = this.source.getOffset();
        CFFIndex top = this.readIndex();
        this.definedNames = this.readIndex();
        for (int i = 0; i < name.size(); ++i) {
            fontNames.add(new String(name.get(i)));
            if (isCIDFont(top.get(i))) {
                //TODO: CID fonts
            } else {
                CFFType1SubfontParser parser = new CFFType1SubfontParser(this.source,
                        this.definedNames,
                        topOffset + top.getOffset(i) - 1 + top.getOffsetShift(),
                        topOffset + top.getOffset(i + 1) - 1 + top.getOffsetShift());
                parser.parseFont();
                this.fontParsers.add(parser);
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
            return topDict[rosOffset] == 12 && topDict[rosOffset + 1] == 30;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }
}
