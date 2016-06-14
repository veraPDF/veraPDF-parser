package org.verapdf.parser;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class reads objects from decoded object stream.
 * @author Sergey Shemyakov
 */
public class DecodedObjectStreamParser extends COSParser {

    private COSStream objectStream;
    private Map<Long, Long> internalOffsets;
    private COSKey streamKey;

    /**
     * Constructor from decoded object stream data and COSStream.
     * @param inputStream contains decoded object stream.
     * @param objectStream is COSStream that is being parsed.
     * @param streamKey is key of given COSStream.
     * @throws Exception
     */
    public DecodedObjectStreamParser(final InputStream inputStream,
                                     COSStream objectStream,
                                     COSKey streamKey) throws Exception {
        super(inputStream);
        this.objectStream = objectStream;
        this.internalOffsets = new HashMap<>();
        this.streamKey = streamKey;
        try{
            calculateInternalOffsets();
        } catch (IOException e) {
            throw new Exception("Object stream " + this.streamKey.getNumber() + " "
                    + this.streamKey.getGeneration() + " has invalid N value", e);
        }
    }

    private void calculateInternalOffsets() throws IOException {
        int n = (int) ((COSInteger) this.objectStream.getKey(ASAtom.N).get()).get();
        for(int i = 0; i < n; ++i) {
            Long objNum, objOffset;
            skipSpaces(false);
            readNumber();
            objNum = getToken().integer;
            skipSpaces(false);
            readNumber();
            objOffset = getToken().integer;
            internalOffsets.put(objNum, objOffset);
        }
    }

    /**
     * @return true if object stream contains object with number <code>objNum</code>.
     */
    public boolean containsObject(long objNum) {
        return this.internalOffsets.containsKey(objNum);
    }

    /**
     * @return list of keys of all objects, contained inside this object stream.
     */
    public List<COSKey> getInternalObjectsKeys() {
        List<COSKey> res = new ArrayList<>();
        for(Map.Entry<Long, Long> entry : internalOffsets.entrySet()) {
            res.add(new COSKey(entry.getKey().intValue(), 0));  // Object inside streams shall have generation 0
        }
        return res;
    }

    public COSObject getObject
}
