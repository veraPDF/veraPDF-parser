package org.verapdf.cos;

import org.verapdf.as.ASAtom;

import java.io.IOException;

/**
 * Created by Timur on 12/17/2015.
 */
public class COSIndirect extends COSBase {

    private COSKey key;
    private COSDocument document;
    private COSObject child;

    protected COSIndirect() throws Exception {
        super();
        this.key = new COSKey();
        this.document = new COSDocument(null);
        this.child = new COSObject();
    }

    protected COSIndirect(final COSKey key, final COSDocument document) {
        super();
        this.key = key;
        this.document = document;
        this.child = new COSObject();
    }

    protected COSIndirect(final COSObject value, final COSDocument document) throws Exception {
        super();
        this.key = new COSKey();
        this.document = document;
        this.child = new COSObject();

        if (document == null) {
            this.child = value;
        } else {
            this.key = this.document.setObject(value);
        }
    }

    // OBJECT TYPE
    public COSObjType getType() throws IOException {
        return getDirect().getType();
    }

    public static COSObject construct(final COSKey value) {
        return construct(value, null);
    }

    public static COSObject construct(final COSKey value, final COSDocument doc) {
        return new COSObject(new COSIndirect(value, doc));
    }

    public static COSObject construct(final COSObject value) throws Exception {
        return construct(value, null);
    }

    public static COSObject construct(final COSObject value, final COSDocument doc) throws Exception {
        return new COSObject(new COSIndirect(value, doc));
    }

    //! Boolean values
    public boolean getBoolean() throws IOException {
        return getDirect().getBoolean();
    }

    public boolean setBoolean(final boolean value) throws IOException {
        getDirect().setBoolean(value);
        return true;
    }

    //! Integer numbers
    public long getInteger() throws IOException {
        return getDirect().getInteger();
    }

    public boolean setInteger(final long value) throws IOException {
        getDirect().setInteger(value);
        return true;
    }

    //! Real numbers
    public double getReal() throws IOException {
        return getDirect().getReal();
    }

    public boolean setReal(final double value) throws IOException {
        getDirect().setReal(value);
        return true;
    }

    //! Strings
    public String getString() throws IOException {
        return getDirect().getString();
    }

    public boolean setString(final String value) throws IOException {
        return setString(value, false);
    }

    public boolean setString(final String value, final boolean isHex) throws IOException {
        getDirect().setString(value);
        return true;
    }

    //! Names
    public ASAtom getName() throws IOException {
        return getDirect().getName();
    }

    public boolean setName(final ASAtom value) throws IOException {
        getDirect().setName(value);
        return true;
    }

    //! Number of elements for array and dictionary
    public int size() throws IOException {
        return getDirect().size();
    }

    //! Arrays

    public COSObject at(final int i) throws IOException {
        return getDirect().at(i);
    }

    public boolean add(final COSObject value) throws IOException {
        getDirect().add(value);
        return true;
    }

    public boolean set(final int i, final COSObject value) throws IOException {
        getDirect().set(i, value);
        return true;
    }

    public boolean insert(final int i, final COSObject value) throws IOException {
        getDirect().insert(i, value);
        return true;
    }

    public void remove(final int i) throws IOException {
        getDirect().remove(i);
    }

    public boolean setArray() throws IOException {
        getDirect().setArray();
        return true;
    }

    public boolean setArray(final int size, final COSObject[] value) throws IOException {
        getDirect().setArray(size, value);
        return true;
    }

    public boolean setArray(final int size, final double[] value) throws IOException {
        getDirect().setArray(size, value);
        return true;
    }

    public void clearArray() throws IOException {
        getDirect().clear();
    }

    //! Dictionaries
    public boolean knownKey(final ASAtom key) throws IOException {
        return getDirect().knownKey(key);
    }

    public COSObject getKey(final ASAtom key) throws IOException {
        return getDirect().getKey(key);
    }

    public boolean setKey(final ASAtom key, final COSObject value) throws IOException {
        getDirect().setKey(key, value);
        return true;
    }

    public boolean getBooleanKey(final ASAtom key) throws IOException {
        return getDirect().getBooleanKey(key);
    }

    public boolean setBooleanKey(final ASAtom key, final boolean value) throws IOException {
        getDirect().setBooleanKey(key, value);
        return true;
    }

    public long getIntegerKey(final ASAtom key) throws IOException {
        return getDirect().getIntegerKey(key);
    }

    public boolean setIntegerKey(final ASAtom key, final long value) throws IOException {
        getDirect().setIntegerKey(key, value);
        return true;
    }

    public double getRealKey(final ASAtom key) throws IOException {
        return getDirect().getRealKey(key);
    }

    public boolean setRealKey(final ASAtom key, final double value) throws IOException {
        getDirect().setRealKey(key, value);
        return true;
    }

    public String getStringKey(final ASAtom key) throws IOException {
        return getDirect().getStringKey(key);
    }

    public boolean setStringKey(final ASAtom key, final String value) throws IOException {
        getDirect().setStringKey(key, value);
        return true;
    }

    public ASAtom getNameKey(final ASAtom key) throws IOException {
        return getDirect().getNameKey(key);
    }

    public boolean setNameKey(final ASAtom key, final ASAtom value) throws IOException {
        getDirect().setNameKey(key, value);
        return true;
    }

    public boolean setArrayKey(final ASAtom key) throws IOException {
        getDirect().setArrayKey(key);
        return true;
    }

    public boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value) throws IOException {
        getDirect().setArrayKey(key, size, value);
        return true;
    }

    public boolean setArrayKey(final ASAtom key, final int size, final double[] value) throws IOException {
        getDirect().setArrayKey(key, size, value);
        return true;
    }

    public void removeKey(final ASAtom key) throws IOException {
        getDirect().removeKey(key);
    }

    //! Indirect object
    public boolean isIndirect() {
        return true;
    }

    public COSKey getKey() {
        return this.key;
    }

    public COSDocument getDocument() {
        return this.document;
    }

    public boolean setKey(final COSKey key, final COSDocument document) {
        this.key = key;
        this.document = document;
        return true;
    }

    public COSObject getDirect() throws IOException {
        return this.document != null ? this.document.getObject(key) : this.child;
    }

    public boolean setDirect(final COSObject value) {
        if (this.document != null) {
            this.document.setObject(this.key, value);
        } else {
            this.child = value;
        }
        return true;
    }

    //! Marks object for incremental update.
    //! (If object is indirect and its document is known.)
    public void mark() throws IOException {
        if (this.document != null) {
            COSObject object = new COSObject(this);
            this.document.setObject(object);
        }
    }

}
