package org.verapdf.pd.structure;

import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 *
 *
 * @author Sergey Shemyakov
 */
public class NameSpaceRoleMapping {

    private COSDictionary dictionary;

    public NameSpaceRoleMapping(COSDictionary dictionary) {
        this.dictionary = dictionary == null ? (COSDictionary) COSDictionary.construct().get() : dictionary;
    }

    public COSName getNameInDefault(COSName name) {
        if (name != null) {
            COSObject object = dictionary.getKey(name.get());
            if (object != null) {
                if (object.getType() == COSObjType.COS_NAME) {
                    return (COSName) object.get();
                } else if (object.getType() == COSObjType.COS_ARRAY && object.size() >= 2) {
                    PDStructureElementNameSpace nameSpace2 = NameSpaceFactory.getNameSpace(object.at(1));
                    COSObject name2 = object.at(0);
                    if (nameSpace2 != null && name2.getType() == COSObjType.COS_NAME) {
                        NameSpaceRoleMapping mapping2 = nameSpace2.getNameSpaceMapping();
                        if (mapping2.dictionary != this.dictionary || name2.getName() != name.getName()) {
                            return nameSpace2.getNameSpaceMapping().getNameInDefault((COSName) name2.get());
                        }
                    }
                }
            }
        }
        return null;
    }
}
