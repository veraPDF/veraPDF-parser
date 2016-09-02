package org.verapdf.pd.font.type1;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.io.ASMemoryInStream;
import org.verapdf.parser.COSParser;
import org.verapdf.pd.font.PDFont;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Sergey Shemyakov
 */
public class PDType1Font extends PDFont {

    private static final Logger LOGGER = Logger.getLogger(PDType1Font.class);

    public PDType1Font(COSDictionary dictionary) {
        super(dictionary);
    }

    public Set<String> getDescriptorCharSet() {
        String descriptorCharSetString =
                this.fontDescriptor.getStringKey(ASAtom.CHAR_SET);
        if (descriptorCharSetString != null) {
            try {
                ASMemoryInStream stream =
                        new ASMemoryInStream(descriptorCharSetString.getBytes());
                Set<String> descriptorCharSet = new TreeSet<>();
                COSParser parser = new COSParser(stream);
                COSObject glyphName = parser.nextObject();
                while (!glyphName.empty()) {
                    if (glyphName.getType() == COSObjType.COS_NAME) {
                        descriptorCharSet.add(glyphName.getString());
                    }
                    glyphName = parser.nextObject();
                }
                return descriptorCharSet;
            } catch (IOException ex) {
                LOGGER.error("Can't parse /CharSet entry in Type 1 font descriptor");
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();
    }

}
