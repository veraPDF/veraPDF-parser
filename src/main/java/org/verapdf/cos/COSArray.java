package org.verapdf.cos;

import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.util.*;

/**
 * @author Timur Kamalov
 */
public class COSArray extends COSDirect implements Iterable<COSObject> {

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
        return COSObjType.COS_ARRAY;
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

    public void accept(IVisitor visitor) {
        visitor.visitFromArray(this);
    }

    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromArray(this);
    }

    public Integer size() {
        return this.entries.size();
    }

    //TODO : cosbase?
    public Iterator<COSObject> iterator() {
        return this.entries.iterator();
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if(obj instanceof COSObject) {
            return this.equals(((COSObject) obj).get());
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        List<COSBasePair> checkedObjects = new LinkedList<COSBasePair>();
        return this.equals(obj, checkedObjects);

    }

    boolean equals(Object obj, List<COSBasePair> checkedObjects) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if(obj instanceof COSObject) {
            return this.equals(((COSObject) obj).get());
        }
        if (COSBasePair.listContainsPair(checkedObjects, this, (COSBase) obj)) {
            return true;    // Not necessary true, but we should behave as it is
        }
        COSBasePair.addPairToList(checkedObjects, this, (COSBase) obj);
        if (getClass() != obj.getClass()) {
            return false;
        }
        COSArray that = (COSArray) obj;
        if (!that.size().equals(this.size())) {
            return false;
        }
        for (int i = 0; i < this.size(); ++i) {
            COSBase cosBase1 = this.at(i).getDirectBase();
            COSBase cosBase2 = that.at(i).getDirectBase();
            if (!cosBase1.equals(cosBase2, checkedObjects)) {
                return false;
            }
        }
        return true;
    }
}
