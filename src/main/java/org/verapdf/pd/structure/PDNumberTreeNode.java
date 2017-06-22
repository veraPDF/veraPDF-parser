package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

import java.util.*;

/**
 * Class implements number tree structure (see 7.9.7 in PDF 32000_2008).
 *
 * @author Sergey Shemyakov
 */
public class PDNumberTreeNode extends PDObject {

    /**
     * Constructor from number tree node dictionary.
     *
     * @param obj is a number tree node dictionary.
     */
    public PDNumberTreeNode(COSObject obj) {
        super(obj);
    }

    /**
     * @return array of two numbers representing limits of this node or null if
     * proper limits array is not present.
     */
    public long[] getLimitsArray() {
        COSObject limits = this.getKey(ASAtom.LIMITS);
        if (limits != null && !limits.empty() && limits.getType() == COSObjType.COS_ARRAY
                && limits.size() >= 2) {
            long[] res = new long[2];
            res[0] = limits.at(0).getInteger();
            res[1] = limits.at(1).getInteger();
            return res;
        }
        return null;
    }

    /**
     * @return the list of number tree nodes that are kids of this node or null
     * if no kids are present.
     */
    public List<PDNumberTreeNode> getKids() {
        COSObject kids = this.getKey(ASAtom.KIDS);
        if (kids != null && !kids.empty() && kids.getType() == COSObjType.COS_ARRAY) {
            List<PDNumberTreeNode> res = new ArrayList<>(kids.size());
            for (COSObject obj : (COSArray) kids.get()) {
                res.add(new PDNumberTreeNode(obj));
            }
            return Collections.unmodifiableList(res);
        }
        return null;
    }

    /**
     * @return map from numbers to objects that is represented by this node or
     * null if nums are not present.
     * TODO: test method
     */
    public Map<Long, COSObject> getNums() {
        COSObject nums = this.getKey(ASAtom.NUMS);
        if (nums != null && !nums.empty() && nums.getType() == COSObjType.COS_ARRAY) {
            Map<Long, COSObject> res = new HashMap<>();
            for (int i = 0; i < nums.size() - 1; i += 2) { // size - 1 checks case with odd amount of entries in array
                COSObject key = nums.at(i);
                if (key.getType() == COSObjType.COS_INTEGER) {
                    COSObject value = nums.at(i + 1);
                    res.put(key.getInteger(), value);
                }
            }
            return Collections.unmodifiableMap(res);
        }
        return null;
    }

    /**
     * Gets object with given key from this node and it's kids recursively.
     *
     * @param key is integer that is a key for COSObject.
     * @return object for given key from this number tree node and it's kids or
     * null if object can't be found.
     */
    public COSObject getObject(Long key) {
        long[] limits = this.getLimitsArray();
        if (limits != null) {
            if (key < limits[0] || key > limits[1]) {
                // integer not in the limits
                return null;
            }
        }

        if (this.knownKey(ASAtom.NUMS)) {
            // just get object from nums or check if it is not in nums
            Map<Long, COSObject> nums = getNums();
            return nums == null ? null : nums.get(key);
        }

        if (this.knownKey(ASAtom.KIDS)) {
            // find kid with mapping for given key
            List<PDNumberTreeNode> kids = getKids();
            if (kids != null) {
                COSObject res;
                for (PDNumberTreeNode kid : kids) {
                    res = kid.getObject(key);
                    if (res != null) {
                        return res;
                    }
                }
            }
        }

        return null;
    }
}
