package org.verapdf.font.type1;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.filters.COSFilterASCIIHexDecode;
import org.verapdf.io.ASMemoryInStream;
import org.verapdf.parser.COSParser;
import org.verapdf.parser.Token;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class does parsing of Type 1 font files.
 *
 * @author Sergey Shemyakov
 */
public class Type1Parser extends COSParser {

    private double[] fontMatrix = {0.001, 0, 0, 0.001, 0, 0};
    private Map<Integer, String> encoding;

    /**
     * {@inheritDoc}
     */
    public Type1Parser(String fileName) throws IOException {
        super(fileName);
        encoding = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    public Type1Parser(InputStream fileStream) throws IOException {
        super(fileStream);
        encoding = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    public Type1Parser(ASInputStream asInputStream) throws IOException {
        super(asInputStream);
        encoding = new HashMap<>();
    }

    /**
     * This method is entry point for parsing process.
     *
     * @throws IOException
     */
    public void parse() throws IOException {
        initializeToken();

        skipSpaces(true);

        while (getToken().type != Token.Type.TT_EOF) {
            nextToken();
            processToken();
        }
    }

    private void processToken() throws IOException {
        switch (getToken().type) {
            case TT_NAME:
                switch (getToken().token) {
                    //Do processing of all necessary names like /FontName, /FamilyName, etc.
                    case "FontMatrix":
                        this.skipSpaces();
                        this.nextToken();
                        if(this.getToken().type == Token.Type.TT_OPENARRAY) {
                            COSObject cosFontMatrix = this.nextObject();
                            if(cosFontMatrix.size() == 6) {
                                for (int i = 0; i < 6; ++i) {
                                    fontMatrix[i] = cosFontMatrix.at(i).getReal();
                                }
                            }
                        }
                        break;
                    case "Encoding":
                        do {
                            nextToken();
                        } while (!this.getToken().token.equals("dup"));
                        this.source.unread(3);

                        while(true) {
                            nextToken();
                            if(this.getToken().token.equals("readonly")) {
                                break;
                            }
                            this.skipSpaces();
                            this.readNumber();
                            long key = this.getToken().integer;
                            this.nextToken();
                            encoding.put((int) key, this.getToken().token);
                            this.nextToken();
                        }
                        break;
                }
                break;
            case TT_NONE:
                switch (getToken().token) {
                    //Do processing of keywords like eexec
                    case "eexec":
                        this.skipSpaces();
                        ASFileInStream eexecEncoded = new ASFileInStream(
                                this.source.getStream(), this.source.getOffset(),
                                this.source.getStreamLength() - this.source.getOffset());
                        ASBufferingInFilter eexecDecoded = new EexecFilterDecode(
                                new COSFilterASCIIHexDecode(eexecEncoded), false);

                        break;
                }
                break;
        }
    }

    private ASBufferingInFilter readEexecData() throws IOException {
        byte ch;
        long dataBeginning = this.source.getOffset();
        ch = this.source.read();
        while (!this.source.isEof()) {
            if (ch == 'c' && source.peek() == 'l') { // Probably got to "cleartomark"
                break;
            }
            ch = this.source.read();
        }
        long dataEnding = this.source.getOffset() - 512;    // Skipping 512 zeroes
        byte[] res = new byte[(int) (dataEnding - dataBeginning)];

        this.source.seek(dataBeginning);
        this.source.read(res, res.length);
        return new COSFilterASCIIHexDecode(new ASMemoryInStream(res, res.length, false));
    }
}
