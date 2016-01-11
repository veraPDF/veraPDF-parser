package org.verapdf.cos;

import java.util.ArrayList;
import java.util.Arrays;
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

    protected COSArray(final int size, final COSObject[] values) {
        super();
        this.entries = Arrays.asList(values);
    }

    protected COSArray(final int size, final double[] values) {
        super();
        this.entries = new ArrayList<COSObject>();
        for (double value : values) {
            this.entries.add(COSReal.construct(value));
        }
    }

    protected COSArray(final int i, final COSObject object) {
        super();
        this.entries = new ArrayList<COSObject>();
        this.entries.add(i, object);
    }

    //! Object type
    public COSObjType getType() {
        return COSObjType.COSArrayT;
    }

    //! Returns COSObject wrapping a new empty COSArray instance
    public static COSObject construct() {
        return new COSObject(new COSArray());
    }

    //! Returns COSObject wrapping a new COSArray instance filled with given values
    public static COSObject construct(final int size, final COSObject[] value) {
        return new COSObject(new COSArray(size, value));
    }

    public static COSObject construct(final int size, final double[] value) {
        return new COSObject(new COSArray(size, value));
    }

    //! Returns COSObject wrapping a new COSArray instance constructed via given object at a given index
    public static COSObject construct(final int i, final COSObject obj) {
        return new COSObject(new COSArray(i, obj));
    }

    public int size() {
        return this.entries.size();
    }

    public COSObject at(final int i) {
        if (i >= this.entries.size()) {
            return new COSObject();
        }

        return _at(i);
    }

    public boolean add(final COSObject value) {
        this.entries.add(value);
        return true;
    }

    public boolean set(final int i, final COSObject value) {
        this.entries.set(i, value);
        return true;
    }

    public boolean insert(final int i, final COSObject value) {
        this.entries.add(i, value);
        return true;
    }

    public void remove(final int i) {
        if (entries.size() < i) {
            this.entries.remove(i);
        }
    }

    public boolean setArray() {
        this.entries.clear();
        return true;
    }

    public boolean setArray(final int size, final COSObject[] value) {
        //TODO : check this
        this.entries.addAll(Arrays.asList(value));
        return true;
    }

    public boolean setArray(final int size, final double[] values) {
        this.entries.clear();
        for (double value : values) {
            this.entries.add(COSReal.construct(value));
        }
        return true;
    }

    public void clearArray() {
        this.entries.clear();
    }

    private COSObject _at(final int i) {
        return this.entries.get(i);
    }

}
