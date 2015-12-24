package org.verapdf.io;

import org.verapdf.cos.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Timur Kamalov
 */
public class PDFParser extends Parser {

    private COSDocument document;
    private Queue<COSObject> objects;
    private Queue<Long> integers;
    private boolean flag;

    public PDFParser(final String filename) throws FileNotFoundException {
        super(filename);
        this.document = new COSDocument(null);
        this.objects = new LinkedList<COSObject>();
        this.flag = true;
    }

    public PDFParser(final COSDocument document, final String filename) throws FileNotFoundException { //tmp ??
        this(filename);
        this.document = document;
    }

    public String getHeader() throws IOException {
        return getLine(0);
    }

    public void getXRefInfo(List<COSXRefInfo> infos) {
        this.getXRefInfo(infos, 0);
    }

    public COSObject nextObject() throws IOException {
        if (!this.objects.isEmpty()) {
            COSObject result = this.objects.peek();
            this.objects.remove();
            return result;
        }

        if (this.flag) {
            nextToken();
        }
        this.flag = true;

        final Token token = getToken();

        if (token.type == Token.Type.TT_INTEGER) {  // looking for indirect reference
            this.integers.add(token.integer);
            if (this.integers.size() == 3) {
                COSObject result = COSInteger.construct(this.integers.peek());
                this.integers.remove();
                return result;
            }
            return nextObject();
        }

        if (token.type == Token.Type.TT_KEYWORD
                && token.keyword == Token.Keyword.KW_R
                && this.integers.size() == 2) {
            final long number = this.integers.peek();
            this.integers.remove();
            final long generation = this.integers.peek();
            this.integers.remove();
            return COSIndirect.construct(new COSKey(number, generation), document);
        }

        if (!this.integers.isEmpty()) {
            COSObject result = COSInteger.construct(this.integers.peek());
            this.integers.remove();
            while (!this.integers.isEmpty()) {
                this.objects.add(COSInteger.construct(this.integers.peek()));
                this.integers.remove();
            }
            this.flag = false;
            return result;
        }

        switch (token.type) {
            case TT_NONE:
                break;
            case TT_KEYWORD: {
                switch (token.keyword) {
                    case KW_NONE:
                        break;
                    case KW_NULL:
                        return COSNull.construct();
                    case KW_TRUE:
                        return COSBoolean.construct(true);
                    case KW_FALSE:
                        return COSBoolean.construct(false);
                    case KW_STREAM:
                    case KW_ENDSTREAM:
                    case KW_OBJ:
                    case KW_ENDOBJ:
                    case KW_R:
                    case KW_N:
                    case KW_F:
                    case KW_XREF:
                    case KW_STARTXREF:
                    case KW_TRAILER:
                        break;
                }
                break;
            }
            case TT_INTEGER: //should not enter here
                break;
            case TT_REAL:
                return COSReal.construct(token.real);
            case TT_LITSTRING:
                return COSString.construct(token.token);
            case TT_HEXSTRING:
                return COSString.construct(token.token, true);
            case TT_NAME:
                return COSName.construct(token.token);
            case TT_OPENARRAY:
                this.flag = false;
                return getArray();
            case TT_CLOSEARRAY:
                return new COSObject();
            case TT_OPENDICT:
                this.flag = false;
                return getDictionary();
            case TT_CLOSEDICT:
                return new COSObject();
            case TT_EOF:
                return new COSObject();
        }
        return new COSObject();
    }

    private COSObject getArray() throws IOException {
        if (this.flag) {
            nextToken();
        }
        this.flag = true;

        final Token token = getToken();
        if (token.type != Token.Type.TT_OPENARRAY) {
            return new COSObject();
        }

        COSObject arr = COSArray.construct();

        COSObject obj = nextObject();
        while(!obj.empty()) {
            arr.add(obj);
            obj = nextObject();
        }

        if (token.type != Token.Type.TT_CLOSEARRAY) {
            closeInputStream();
            // TODO : replace with ASException
            throw new IOException("PDFParser::GetArray()" + INVALID_PDF_ARRAY);
        }

        return arr;
    }

    private COSObject getName() throws IOException {
        if (this.flag) {
            nextToken();
        }
        this.flag = true;

        final Token token = getToken();
        if (token.type != Token.Type.TT_NAME) {
            return new COSObject();
        }
        return COSName.construct(token.token);
    }

    private COSObject getDictionary() throws IOException {
        if (this.flag) {
            nextToken();
        }
        this.flag = true;
        final Token token = getToken();

        if (token.type != Token.Type.TT_OPENDICT) {
            return new COSObject();
        }

        COSObject dict = COSDictionary.construct();

        COSObject key = getName();
        while(!key.empty()) {
            COSObject obj = nextObject();
            dict.setKey(key.getName(), obj);
            key = getName();
        }

        if (token.type != Token.Type.TT_CLOSEDICT) {
            closeInputStream();
            // TODO : replace with ASException
            throw new IOException("PDFParser::GetDictionary()", INVALID_PDF_DICTONARY);
        }

        if (this.flag) {
            nextToken();
        }
        this.flag = false;

        if (token.type == Token.Type.TT_KEYWORD &&
                token.keyword == Token.Keyword.KW_STREAM) {
            return getStream(dict);
        }

        return dict;
    }



}
