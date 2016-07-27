package org.verapdf.font.cff;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Instance of this class represent a parser of Type1-like font from FontSet of
 * CFF file.
 *
 * @author Sergey Shemyakov
 */
public class CFFType1SubfontParser extends CFFInnerFontParser {

    protected int encodingOffset;
    byte[] encoding;

    public CFFType1SubfontParser(ASInputStream stream) throws IOException {
        super(stream);
        encodingOffset = 0; // default
        encoding = new byte[256];
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

    protected void readEncoding() throws IOException {
        if(encodingOffset == 0) {   // TODO: case of standard encoding

        } else if (encodingOffset == 1) {   // TODO: case of expert encoding

        } else {
            int format = this.readCard8() & 0xFF;
            switch (format) {
                case 0:
                    byte amount = this.readCard8();
                    for(int i = 0; i < amount; ++i) {
                        this.encoding[i] = this.readCard8();
                    }
                    break;
                case 1:

                    break;
                case 128:
                    // like 0 with Supplements
                    break;
                case 129:
                    // like 1 with Supplements
                    break;
            }
        }
    }

}
