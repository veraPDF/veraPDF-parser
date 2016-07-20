package org.verapdf.font.type1;

import org.verapdf.as.CharTable;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.filters.COSFilterASCIIHexDecode;
import org.verapdf.parser.BaseParser;
import org.verapdf.parser.Token;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * This class does parsing of Type 1 font files.
 *
 * @author Sergey Shemyakov
 */
public class Type1Parser extends BaseParser {

    /**
     * {@inheritDoc}
     */
    public Type1Parser(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    /**
     * {@inheritDoc}
     */
    public Type1Parser(InputStream fileStream) throws IOException {
        super(fileStream);
    }

    /**
     * {@inheritDoc}
     */
    public Type1Parser(ASInputStream asInputStream) throws IOException {
        super(asInputStream);
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
                }
                break;
            case TT_NONE:
                switch (getToken().token) {
                    //Do processing of keywords like eexec
                    case "eexec":
                        this.skipSpaces();
                        this.readEexecData();   //TODO: do something with this data.

                }
                break;
        }
    }

    private byte[] readEexecData() throws IOException {
        byte ch;
        long dataBeginning = this.source.getOffset();
        ch = this.source.read();
        while (!this.source.isEof()) {
            if (ch == 'c' && source.peek() == 'l') { // Probably got to "clearmark"
                break;
            }
            ch = this.source.read();
        }
        long dataEnding = this.source.getOffset() - 512;    // Skipping 512 zeroes
        byte[] res = new byte[(int) (dataEnding - dataBeginning + 1) / 2];
        int resPointer = 0;

        this.source.seek(dataBeginning);
        while (this.source.getOffset() < dataEnding) {
            res[resPointer++] = this.readASCIIHexByte();
        }
        return Arrays.copyOf(res, resPointer);
    }

    private byte readASCIIHexByte() throws IOException {
        byte res = 0;
        byte ch = this.source.read();
        while (CharTable.isSpace(ch)) {
            ch = this.source.read();
        }
        if (COSFilterASCIIHexDecode.decodeLoHex(ch) == COSFilterASCIIHexDecode.er) {
            throw new IOException("Corrupted ASCII Hex string in eexec encoded data");
        }
        res = COSFilterASCIIHexDecode.decodeLoHex(ch);
        res = (byte) (res << 4);

        ch = this.source.read();
        while (CharTable.isSpace(ch)) {
            ch = this.source.read();
        }

        if (COSFilterASCIIHexDecode.decodeLoHex(ch) == COSFilterASCIIHexDecode.er) {
            throw new IOException("Corrupted ASCII Hex string in eexec encoded data");
        }
        res += COSFilterASCIIHexDecode.decodeLoHex(ch);
        return res;
    }
}
