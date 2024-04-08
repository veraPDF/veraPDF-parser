/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd.structure;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.exceptions.LoopedException;
import org.verapdf.pd.PDObject;

import java.util.*;

/**
 * Class implements number tree structure (see 7.9.7 in PDF 32000_2008).
 *
 * @author Sergey Shemyakov
 */
public class PDNumberTreeNode extends PDObject implements Iterable<COSObject> {

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
                && limits.size() >= 2 && limits.at(0).getType() == COSObjType.COS_INTEGER
                && limits.at(1).getType() == COSObjType.COS_INTEGER) {
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
        return Collections.emptyList();
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
        return Collections.emptyMap();
    }

    private List<COSObject> getObjects() {
        List<COSObject> result = new LinkedList<>(getNums().values());
        for (PDNumberTreeNode kid : getKids()) {
            result.addAll(kid.getObjects());
        }
        return result;
    }

    /**
     * Gets object with given key from this node and it's kids recursively.
     *
     * @param key is integer that is a key for COSObject.
     * @return object for given key from this number tree node and it's kids or
     * null if object can't be found.
     */
    public COSObject getObject(Long key) {
        Set<COSKey> visitedKeys = new HashSet<>();
        COSKey objectKey = getObject().getObjectKey();
        if (objectKey != null) {
            visitedKeys.add(objectKey);
        }
        return getObject(key, visitedKeys);
    }

    private COSObject getObject(Long key, Set<COSKey> visitedKeys) {
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
                for (PDNumberTreeNode kid : kids) {
                    COSKey kidObjectKey = kid.getObject().getObjectKey();
                    if (kidObjectKey != null) {
                        if (visitedKeys.contains(kidObjectKey)) {
                            throw new LoopedException("Loop inside number tree");
                        } else {
                            visitedKeys.add(kidObjectKey);
                        }
                    }
                    COSObject res = kid.getObject(key, visitedKeys);
                    if (res != null) {
                        return res;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Iterator<COSObject> iterator() {
        return getObjects().iterator();
    }

    public Long size() {
        long i = 0;
        Iterator<COSObject> iterator = iterator();
        for (; iterator.hasNext(); i++) {
            iterator.next();
        }
        return i;
    }
}
