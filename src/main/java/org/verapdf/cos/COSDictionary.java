package org.verapdf.cos;

import org.verapdf.as.ASAtom;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    protected COSDictionary(final ASAtom key, final boolean value) throws IOException {
        this();
        setBooleanKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final int value) throws IOException {
        this();
        setIntegerKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final double value) throws IOException {
        this();
        setRealKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final String value) throws IOException {
        this();
        setStringKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final ASAtom value) throws IOException {
        this();
        setNameKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final int size, final COSObject[] value) throws IOException {
        this();
        setArrayKey(key, size, value);
    }

    protected COSDictionary(final ASAtom key, final int size, final double[] value) throws IOException {
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

    public static COSObject construct(final ASAtom key, final boolean value) throws IOException {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final int value) throws IOException {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final double value) throws IOException {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final String value) throws IOException {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final ASAtom value) throws IOException {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final int size, final COSObject[] value) throws IOException {
        return new COSObject(new COSDictionary(key, size, value));
    }

    public static COSObject construct(final ASAtom key, final int size, final double[] value) throws IOException {
        return new COSObject(new COSDictionary(key, size, value));
    }

    public static COSObject construct(final COSDictionary dict) {
        return new COSObject(new COSDictionary(dict));
    }

    public int size() {
        return this.entries.size();
    }

    public boolean knownKey(final ASAtom key) {
        return this.entries.containsKey(key);
    }

    public COSObject getKey(final ASAtom key) {
        //TODO : don't even think about leaving this nightmare in code
        //TODO : rewrite ASAtom using Enum and everyone will be happy
        //COSObject value = this.entries.get(key);
        COSObject value = null;
        for (Map.Entry<ASAtom, COSObject> entry : this.entries.entrySet()) {
            if (entry.getKey().get().equals(key.get())) {
                value = entry.getValue();
                break;
            }
        }
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

    public boolean getBooleanKey(final ASAtom key) throws IOException {
        return getKey(key).getBoolean();
    }

    public boolean setBooleanKey(final ASAtom key, final boolean value) throws IOException {
        COSObject obj = new COSObject();
        obj.setBoolean(value);
        this.entries.put(key, obj);
        return true;
    }

    public long getIntegerKey(final ASAtom key) throws IOException {

        return getKey(key).getInteger();
    }

    public boolean setIntegerKey(final ASAtom key, final long value) throws IOException {
        COSObject obj = new COSObject();
        obj.setInteger(value);
        this.entries.put(key, obj);
        return true;
    }

    public double getRealKey(final ASAtom key) throws IOException {
        return getKey(key).getReal();
    }

    public boolean setRealKey(final ASAtom key, final double value) throws IOException {
        COSObject obj = new COSObject();
        obj.setReal(value);
        this.entries.put(key, obj);
        return true;
    }

    public String getStringKey(final ASAtom key) throws IOException {
        return getKey(key).getString();
    }

    public boolean setStringKey(final ASAtom key, final String value) throws IOException {
        COSObject obj = new COSObject();
        obj.setString(value);
        this.entries.put(key, obj);
        return true;
    }

    public final ASAtom getNameKey(final ASAtom key) throws IOException {
        return getKey(key).getName();
    }

    public boolean setNameKey(final ASAtom key, final ASAtom value) throws IOException {
        COSObject obj = new COSObject();
        obj.setName(value);
        this.entries.put(key, obj);
        return true;
    }

    public boolean setArrayKey(final ASAtom key) throws IOException {
        COSObject obj = new COSObject();
        obj.setArray();
        this.entries.put(key, obj);
        return true;
    }

    public boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value) throws IOException {
        COSObject obj = new COSObject();
        obj.setArray(size, value);
        this.entries.put(key, obj);
        return true;
    }

    public boolean setArrayKey(final ASAtom key, final int size, final double[] value) throws IOException {
        COSObject obj = new COSObject();
        obj.setArray(size, value);
        this.entries.put(key, obj);
        return true;
    }

    public void removeKey(final ASAtom key) {
        this.entries.remove(key);
    }

}
