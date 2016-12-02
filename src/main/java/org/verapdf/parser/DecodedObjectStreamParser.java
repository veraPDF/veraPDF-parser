package org.verapdf.parser;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class reads objects from decoded object stream.
 *
 * @author Sergey Shemyakov
 */
public class DecodedObjectStreamParser extends COSParser {

    private COSStream objectStream;
    private Map<Integer, Long> internalOffsets;

    /**
     * Constructor from decoded object stream data and COSStream.
     *
     * @param inputStream  contains decoded object stream.
     * @param objectStream is COSStream that is being parsed.
     * @param streamKey    is key of given COSStream.
     * @throws IOException
     */
    public DecodedObjectStreamParser(final ASInputStream inputStream,
                                     COSStream objectStream,
                                     COSKey streamKey, COSDocument doc) throws IOException {
        super(doc, inputStream);
        this.objectStream = objectStream;
        this.internalOffsets = new HashMap<>();
        try {
            calculateInternalOffsets();
        } catch (IOException e) {
            throw new IOException("Object stream " + streamKey.getNumber() + " "
                    + streamKey.getGeneration() + " has invalid N value", e);
        }
    }

    private void calculateInternalOffsets() throws IOException {
        int n = (int) ((COSInteger) this.objectStream.getKey(ASAtom.N).getDirectBase()).get();
        long first = ((COSInteger) this.objectStream.getKey(ASAtom.FIRST).getDirectBase()).get();
        for (int i = 0; i < n; ++i) {
            Long objNum, objOffset;
            skipSpaces(false);
            readNumber();
            objNum = getToken().integer;
            skipSpaces(false);
            readNumber();
            objOffset = getToken().integer;
            internalOffsets.put(objNum.intValue(), objOffset + first);
        }
    }

    /**
     * @return true if object stream contains object with number <code>objNum</code>.
     */
    public boolean containsObject(int objNum) {
        return this.internalOffsets.containsKey(objNum);
    }

    /**
     * @return list of keys of all objects, contained inside this object stream.
     */
    public List<COSKey> getInternalObjectsKeys() {
        List<COSKey> res = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : internalOffsets.entrySet()) {
            res.add(new COSKey(entry.getKey(), 0));  // Object inside streams shall have generation 0
        }
        return res;
    }

    public COSObject getObject(int objNum) throws IOException {
        if (!this.internalOffsets.containsKey(objNum)) {
            return COSObject.getEmpty();
        }
        this.source.seek(internalOffsets.get(objNum));
        this.flag = true;
        return nextObject();
    }
}
