package org.verapdf.cos;

import org.verapdf.as.ASAtom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents embedded file dictionary accessible via EF key in a file
 * specification dictionary (see PDF 32000-2008, table 44).
 *
 * @author Sergey Shemyakov
 */
public class COSEmbeddedFileDict {

    private final COSDictionary dictionary;
    private static final List<ASAtom> DEFINED_FILE_KEYS;

    static {
        DEFINED_FILE_KEYS = new ArrayList<>();
        DEFINED_FILE_KEYS.add(ASAtom.F);
        DEFINED_FILE_KEYS.add(ASAtom.UF);
        DEFINED_FILE_KEYS.add(ASAtom.DOS);
        DEFINED_FILE_KEYS.add(ASAtom.MAC);
        DEFINED_FILE_KEYS.add(ASAtom.UNIX);
    }

    public COSEmbeddedFileDict(COSDictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * @return a list of streams for available embedded files.
     */
    public List<COSStream> getEmbeddedFileStreams() {
        List<COSStream> res = new ArrayList<>();
        for (ASAtom fileKey : DEFINED_FILE_KEYS) {
            COSObject fileStream = dictionary.getKey(fileKey);
            if (!fileStream.empty() && fileStream.getType() == COSObjType.COS_STREAM) {
                res.add((COSStream) fileStream.getDirectBase());
            }
        }
        return Collections.unmodifiableList(res);
    }

}
