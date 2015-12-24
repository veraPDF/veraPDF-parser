package org.verapdf.cos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Timur on 12/23/2015.
 */
public class COSArray extends COSDirect {

    private List<COSObject> entries;

    protected COSArray() {
        super();
        this.entries = new ArrayList<COSObject>();
    }

    protected COSArray(List<COSObject> values) {
        super();
        this.entries = new ArrayList<COSObject>();
        this.entries.addAll(values);
    }

    protected COSArray(final int i, final COSObject object) {
        super();
        this.entries = new ArrayList<COSObject>();
        this.entries.add(i, object);
    }

    public static COSObject construct() {
        return new COSObject(new COSArray());
    }

    public static COSObject construct(final int i, final COSObject obj) {
        return new COSObject(new COSArray(i, obj));
    }

    @Override
    public boolean add(final COSObject object) {
        this.entries.add(object);
        return true;
    }

    private COSObject at(final int i) {
        return this.entries.get(i);
    }


}
