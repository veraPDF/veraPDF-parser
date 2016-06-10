package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Timur on 12/24/2015.
 */
public class COSDictionary extends COSDirect {

    private Map<ASAtom, COSObject> entries;

    protected COSDictionary() {
        super();
        this.entries = new HashMap<ASAtom, COSObject>();
    }

    protected COSDictionary(final ASAtom key, final COSObject value) {
        this();
        setKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final boolean value) {
        this();
        setBooleanKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final int value) {
        this();
        setIntegerKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final double value) {
        this();
        setRealKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final String value) {
        this();
        setStringKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final ASAtom value) {
        this();
        setNameKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final int size, final COSObject[] value) {
        this();
        setArrayKey(key, size, value);
    }

    protected COSDictionary(final ASAtom key, final int size, final double[] value) {
        this();
        setArrayKey(key, size, value);
    }

    protected COSDictionary(final COSDictionary dict) {
        super();
        this.entries = dict.entries;
    }

    public static COSObject construct() {
        return new COSObject(new COSDictionary());
    }

    public static COSObject construct(final ASAtom key, final COSObject value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final boolean value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final int value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final double value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final String value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final ASAtom value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final int size, final COSObject[] value) {
        return new COSObject(new COSDictionary(key, size, value));
    }

    public static COSObject construct(final ASAtom key, final int size, final double[] value) {
        return new COSObject(new COSDictionary(key, size, value));
    }

    public static COSObject construct(final COSDictionary dict) {
        return new COSObject(new COSDictionary(dict));
    }

    public void accept(IVisitor visitor) {
        visitor.visitFromDictionary(this);
    }

    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromDictionary(this);
    }

    public Integer size() {
        return this.entries.size();
    }

    public Boolean knownKey(final ASAtom key) {
        return this.entries.containsKey(key);
    }

    public COSObject getKey(final ASAtom key) {
        COSObject value = this.entries.get(key);
        return value != null ? value : COSObject.getEmpty();
    }

    public boolean setKey(final ASAtom key, final COSObject value) {
        if (value.empty()) {
            this.entries.remove(key);
        } else {
            this.entries.put(key, value);
        }
        return true;
    }

    public Boolean getBooleanKey(final ASAtom key) {
        return getKey(key).getBoolean();
    }

    public boolean setBooleanKey(final ASAtom key, final boolean value) {
        COSObject obj = new COSObject();
        obj.setBoolean(value);
        this.entries.put(key, obj);
        return true;
    }

    public Long getIntegerKey(final ASAtom key) {

        return getKey(key).getInteger();
    }

    public boolean setIntegerKey(final ASAtom key, final long value) {
        COSObject obj = new COSObject();
        obj.setInteger(value);
        this.entries.put(key, obj);
        return true;
    }

    public Double getRealKey(final ASAtom key) {
        return getKey(key).getReal();
    }

    public boolean setRealKey(final ASAtom key, final double value) {
        COSObject obj = new COSObject();
        obj.setReal(value);
        this.entries.put(key, obj);
        return true;
    }

    public String getStringKey(final ASAtom key) {
        return getKey(key).getString();
    }

    public boolean setStringKey(final ASAtom key, final String value) {
        COSObject obj = new COSObject();
        obj.setString(value);
        this.entries.put(key, obj);
        return true;
    }

    public final ASAtom getNameKey(final ASAtom key) {
        return getKey(key).getName();
    }

    public boolean setNameKey(final ASAtom key, final ASAtom value) {
        COSObject obj = new COSObject();
        obj.setName(value);
        this.entries.put(key, obj);
        return true;
    }

    public boolean setArrayKey(final ASAtom key) {
        COSObject obj = new COSObject();
        obj.setArray();
        this.entries.put(key, obj);
        return true;
    }

    public boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value) {
        COSObject obj = new COSObject();
        obj.setArray(size, value);
        this.entries.put(key, obj);
        return true;
    }

    public boolean setArrayKey(final ASAtom key, final int size, final double[] value) {
        COSObject obj = new COSObject();
        obj.setArray(size, value);
        this.entries.put(key, obj);
        return true;
    }

    public void removeKey(final ASAtom key) {
        this.entries.remove(key);
    }

    // Instead of iterator
    public Set<Map.Entry<ASAtom, COSObject>> getEntrySet() {
        return this.entries.entrySet();
    }

    public Set<ASAtom> getKeySet() {
        return this.entries.keySet();
    }

    public Collection<COSObject> getValues() {
        return this.entries.values();
    }

}
