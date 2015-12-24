package org.verapdf.cos;

import org.verapdf.as.ASAtom;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Timur on 12/24/2015.
 */
public class COSDictionary extends COSDirect {

    private Map<ASAtom, COSObject> entries;

    protected COSDictionary() {
        this.entries = new HashMap<ASAtom, COSObject>();
    }

    public static COSObject construct() {
        return new COSObject(new COSDictionary());
    }

    public boolean setKey(final ASAtom key, final COSObject value) {
        if (value.empty()) {
            this.entries.remove(key);
        } else {
            this.entries.put(key, value);
        }
        return true;
    }

}
