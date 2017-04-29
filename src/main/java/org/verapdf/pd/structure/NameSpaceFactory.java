package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Shemyakov
 */
public class NameSpaceFactory {

    private static Map<String, PDStructureElementNameSpace> storedNameSpaces = new HashMap<>();

    private NameSpaceFactory(){}

    public static void addNameSpace(PDStructureElementNameSpace nameSpace) {
        if (nameSpace != null) {
            COSString name = nameSpace.getNS();
            String key = name == null ? null : name.getString();
            storedNameSpaces.put(key, nameSpace);
        }
    }

    public static PDStructureElementNameSpace getNameSpace(String name) {
        return storedNameSpaces.get(name);
    }

    public static PDStructureElementNameSpace getNameSpace(COSObject cosNameSpace) {
        if (cosNameSpace != null) {
            COSObject ns = cosNameSpace.getKey(ASAtom.NS);
            if (ns != null) {
                String name = ns.getString();
                if (storedNameSpaces.containsKey(name)) {
                    return storedNameSpaces.get(name);
                }
            }
        }
        PDStructureElementNameSpace res = PDStructureElementNameSpace.getNameSpace(cosNameSpace, false);
        addNameSpace(res);
        return res;
    }

    public static boolean containsNameSpace(String name) {
        return storedNameSpaces.containsKey(name);
    }

    public static void clear() {
        storedNameSpaces.clear();
    }

}
