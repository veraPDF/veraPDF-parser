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
            int amount;
            switch (format) {
                case 0:
                case 128:
                    amount = this.readCard8() & 0xFF;
                    for(int i = 0; i < amount; ++i) {
                        this.encoding[i] = this.readCard8();
                    }
                    if(format == 0) {
                        break;
                    }
                    // TODO: supplement
                    break;
                case 1:
                case 129:
                    amount = this.readCard8() & 0xFF;
                    int encodingPointer = 0;
                    for(int i = 0; i < amount; ++i) {
                        int first = this.readCard8() & 0xFF;
                        int nLeft = this.readCard8() & 0xFF;
                        for(int j = 0; j <= nLeft; ++j) {
                            encoding[encodingPointer++] = (byte) (first + j);
                        }
                    }
                    if(format == 1) {
                        break;
                    }
                    // TODO: supplement
                    break;
            }
        }
    }


    public static void main(String[] args) {    //TODO:remove
        byte b = (byte) 250;
        int i = b;
        System.out.println(b); //TODO: remove
    }
}
