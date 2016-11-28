package org.verapdf.cos;

import java.util.List;

/**
 * @author Sergey Shemyakov
 */
public class COSBasePair {
    private COSBase first;
    private COSBase second;

    private COSBasePair(COSBase first, COSBase second) {
        this.first = first;
        this.second = second;
    }

    COSBase getFirst() {
        return first;
    }

    COSBase getSecond() {
        return second;
    }

    private boolean contains(COSBase obj) {
        return first == obj || second == obj;
    }

    static boolean listContainsPair(List<COSBasePair> list, COSBase obj1, COSBase obj2) {
        for (COSBasePair pair : list) {
            if (pair.contains(obj1) && pair.contains(obj2)) {
                return true;
            }
        }
        return false;
    }

    static void addPairToList(List<COSBasePair> list, COSBase obj1, COSBase obj2) {
        if (obj1 instanceof COSArray || obj1 instanceof COSDictionary) {
            list.add(new COSBasePair(obj1, obj2));
        }
    }
}
