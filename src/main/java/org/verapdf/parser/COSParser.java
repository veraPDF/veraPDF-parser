package org.verapdf.parser;

import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.cos.*;
import org.verapdf.parser.postscript.PSObject;
import org.verapdf.pd.encryption.StandardSecurityHandler;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class COSParser {

    private static final Logger LOGGER = Logger.getLogger(COSParser.class.getCanonicalName());

    protected COSDocument document;
    protected Queue<COSObject> objects = new LinkedList<>();
    protected Queue<Long> integers = new LinkedList<>();
    protected COSKey keyOfCurrentObject;
    
    private final BaseParser baseParser;

    protected boolean flag = true;

    public COSParser(BaseParser baseParser) {
        this.baseParser = baseParser;
    }

    public BaseParser getBaseParser() {
        return baseParser;
    }

    /**
     * Gets next object from the source.
     *
     * @return next COSObject.
     */
    public COSObject nextObject() throws IOException {
        if (!this.objects.isEmpty()) {
            COSObject result = this.objects.peek();
            this.objects.remove();
            return result;
        }

        if (this.flag) {
            getBaseParser().initializeToken();
            getBaseParser().nextToken();
        }
        this.flag = true;

        final Token token = getBaseParser().getToken();

        if (token.type == Token.Type.TT_INTEGER) {  // looking for indirect reference
            this.integers.add(Long.valueOf(token.integer));
            if (this.integers.size() == 3) {
                COSObject result = COSInteger.construct(this.integers.peek().longValue());
                this.integers.remove();
                return result;
            }
            return nextObject();
        }

        if (token.type == Token.Type.TT_KEYWORD
                && token.keyword == Token.Keyword.KW_R
                && this.integers.size() == 2) {
            final int number = this.integers.peek().intValue();
            this.integers.remove();
            final int generation = this.integers.peek().intValue();
            this.integers.remove();
            return COSIndirect.construct(new COSKey(number, generation), document);
        }

        if (!this.integers.isEmpty()) {
            COSObject result = COSInteger.construct(this.integers.peek().longValue());
            this.integers.remove();
            while (!this.integers.isEmpty()) {
                this.objects.add(COSInteger.construct(this.integers.peek().longValue()));
                this.integers.remove();
            }
            this.flag = false;
            return result;
        }

        switch (token.type) {
            case TT_NONE:
                break;
            case TT_KEYWORD: {
                if (token.keyword == null && getBaseParser().isPSParser()) {
                    return PSObject.getPSObject(COSName.construct(token.getValue()), true);
                } else if (token.keyword == null) {
                    break;
                }
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
                    default:

                }
                break;
            }
            case TT_INTEGER: //should not enter here
                break;
            case TT_REAL:
                return COSReal.construct(token.real);
            case TT_LITSTRING:
                return COSString.construct(token.getByteValue());
            case TT_HEXSTRING:
                COSObject res = COSString.construct(token.getByteValue(), true,
                        token.getHexCount().longValue(), token.isContainsOnlyHex());
                if (this.document == null || !this.document.isEncrypted()) {
                    return res;
                }
                return this.decryptCOSString(res);
            case TT_NAME:
                return COSName.construct(token.getValue());
            case TT_OPENARRAY:
                this.flag = false;
                return getArray();
            case TT_CLOSEARRAY:
                return new COSObject();
            case TT_OPENDICT:
                this.flag = false;
                return getDictionary();
            case TT_CLOSEDICT:
                return getCloseDictionary();
            case TT_EOF:
                return new COSObject();
            case TT_STARTPROC:
                this.flag = false;
                COSObject proc = getArray();
                return PSObject.getPSObject(proc, true);
        }
        return new COSObject();
    }

    protected COSObject getArray() throws IOException {
        if (this.flag) {
            getBaseParser().nextToken();
        }
        this.flag = true;

        final Token token = getBaseParser().getToken();
        if (token.type != Token.Type.TT_OPENARRAY && !(getBaseParser().isPSParser() && token.type == Token.Type.TT_STARTPROC)) {
            return new COSObject();
        }

        COSObject arr = COSArray.construct();

        COSObject obj = nextObject();
        while (!obj.empty()) {
            arr.add(obj);
            obj = nextObject();
        }

        if (token.type != Token.Type.TT_CLOSEARRAY && !(getBaseParser().isPSParser() && token.type == Token.Type.TT_ENDPROC)) {
            // TODO : replace with ASException
            throw new IOException(getErrorMessage(StringExceptions.INVALID_PDF_ARRAY));
        }

        return arr;
    }

    protected COSObject getName() throws IOException {
        if (this.flag) {
            getBaseParser().nextToken();
        }
        this.flag = true;

        final Token token = getBaseParser().getToken();
        if (token.type != Token.Type.TT_NAME) {
            return new COSObject();
        }
        return COSName.construct(token.getValue());
    }

    protected COSObject getCloseDictionary() {
        return new COSObject();
    }

    protected COSObject getDictionary() throws IOException {
        if (this.flag) {
            getBaseParser().nextToken();
        }
        this.flag = true;
        final Token token = getBaseParser().getToken();

        if (token.type != Token.Type.TT_OPENDICT) {
            return new COSObject();
        }

        COSObject dict = COSDictionary.construct();

        COSObject key = getName();
        while (!key.empty()) {
            COSObject obj = nextObject();
            if (dict.getKeySet().contains(key.getName())) {
                LOGGER.log(Level.WARNING, getErrorMessage("Dictionary/Stream contains duplicated key " + key));
            }
            dict.setKey(key.getName(), obj);
            key = getName();
        }

        if (token.type != Token.Type.TT_CLOSEDICT) {
            // TODO : replace with ASException
            throw new IOException(getErrorMessage(StringExceptions.INVALID_PDF_DICTONARY));
        }

        // Don't parse COSStreams here.

        return dict;
    }

    private COSObject decryptCOSString(COSObject string) {
        StandardSecurityHandler ssh = this.document.getStandardSecurityHandler();
        try {
            ssh.decryptString((COSString) string.getDirectBase(), this.keyOfCurrentObject);
            return string;
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.log(Level.WARNING, getErrorMessage("Can't decrypt string"));
            return string;
        }
    }

    protected String getErrorMessage(String message) {
        if (keyOfCurrentObject != null) {
            return message + "(object key = " + keyOfCurrentObject + ")";
        }
        return getBaseParser().getErrorMessage(message);
    }

    public BaseParserInputStream getSource() {
        return getBaseParser().getSource();
    }
}
