package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.util.Collection;
import java.util.Set;

/**
 * @author Timur Kamalov
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

    protected COSIndirect(final COSObject value, final COSDocument document) {
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

    // Access to base underlying object
    public COSBase get() {
        return this.child.get();
    }

    // OBJECT TYPE
    public COSObjType getType() {
        return getDirect().getType();
    }

    public static COSObject construct(final COSKey value) {
        return construct(value, null);
    }

    public static COSObject construct(final COSKey value, final COSDocument doc) {
        return new COSObject(new COSIndirect(value, doc));
    }

    public static COSObject construct(final COSObject value) {
        return construct(value, null);
    }

    public static COSObject construct(final COSObject value, final COSDocument doc) {
        return new COSObject(new COSIndirect(value, doc));
    }

    public void accept(final IVisitor visitor) {
        visitor.visitFromIndirect(this);
    }

    public Object accept(final ICOSVisitor visitor) {
        return get() != null ? get().accept(visitor) : COSNull.NULL.accept(visitor);
    }

    //! Boolean values
    public Boolean getBoolean() {
        return getDirect().getBoolean();
    }

    public boolean setBoolean(final boolean value) {
        getDirect().setBoolean(value);
        return true;
    }

    //! Integer numbers
    public Long getInteger() {
        return getDirect().getInteger();
    }

    public boolean setInteger(final long value) {
        getDirect().setInteger(value);
        return true;
    }

    //! Real numbers
    public Double getReal() {
        return getDirect().getReal();
    }

    public boolean setReal(final double value) {
        getDirect().setReal(value);
        return true;
    }

    //! Strings
    public String getString() {
        return getDirect().getString();
    }

    public boolean setString(final String value) {
        return setString(value, false);
    }

    public boolean setString(final String value, final boolean isHex) {
        getDirect().setString(value);
        return true;
    }

    //! Names
    public ASAtom getName() {
        return getDirect().getName();
    }

    public boolean setName(final ASAtom value) {
        getDirect().setName(value);
        return true;
    }

    //! Number of elements for array and dictionary
    public Integer size() {
        return getDirect().size();
    }

    //! Arrays

    public COSObject at(final int i) {
        return getDirect().at(i);
    }

    public boolean add(final COSObject value) {
        getDirect().add(value);
        return true;
    }

    public boolean set(final int i, final COSObject value) {
        getDirect().set(i, value);
        return true;
    }

    public boolean insert(final int i, final COSObject value) {
        getDirect().insert(i, value);
        return true;
    }

    public void remove(final int i) {
        getDirect().remove(i);
    }

    public boolean setArray() {
        getDirect().setArray();
        return true;
    }

    public boolean setArray(final int size, final COSObject[] value) {
        getDirect().setArray(size, value);
        return true;
    }

    public boolean setArray(final int size, final double[] value) {
        getDirect().setArray(size, value);
        return true;
    }

    public void clearArray() {
        getDirect().clear();
    }

    //! Dictionaries
    public Boolean knownKey(final ASAtom key) {
        return getDirect().knownKey(key);
    }

    public COSObject getKey(final ASAtom key) {
        return getDirect().getKey(key);
    }

    public boolean setKey(final ASAtom key, final COSObject value) {
        getDirect().setKey(key, value);
        return true;
    }

    public Boolean getBooleanKey(final ASAtom key) {
        return getDirect().getBooleanKey(key);
    }

    public boolean setBooleanKey(final ASAtom key, final boolean value) {
        getDirect().setBooleanKey(key, value);
        return true;
    }

    public Long getIntegerKey(final ASAtom key) {
        return getDirect().getIntegerKey(key);
    }

    public boolean setIntegerKey(final ASAtom key, final long value) {
        getDirect().setIntegerKey(key, value);
        return true;
    }

    public Double getRealKey(final ASAtom key) {
        return getDirect().getRealKey(key);
    }

    public boolean setRealKey(final ASAtom key, final double value) {
        getDirect().setRealKey(key, value);
        return true;
    }

    public String getStringKey(final ASAtom key) {
        return getDirect().getStringKey(key);
    }

    public boolean setStringKey(final ASAtom key, final String value) {
        getDirect().setStringKey(key, value);
        return true;
    }

    public ASAtom getNameKey(final ASAtom key) {
        return getDirect().getNameKey(key);
    }

    public boolean setNameKey(final ASAtom key, final ASAtom value) {
        getDirect().setNameKey(key, value);
        return true;
    }

    public boolean setArrayKey(final ASAtom key) {
        getDirect().setArrayKey(key);
        return true;
    }

    public boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value) {
        getDirect().setArrayKey(key, size, value);
        return true;
    }

    public boolean setArrayKey(final ASAtom key, final int size, final double[] value) {
        getDirect().setArrayKey(key, size, value);
        return true;
    }

    public void removeKey(final ASAtom key) {
        getDirect().removeKey(key);
    }


    public Set<ASAtom> getKeySet() {
        return getDirect().getKeySet();
    }

    public Collection<COSObject> getValues() {
        return getDirect().getValues();
    }

    // STREAMS
    public ASInputStream getData() {
        return this.getData(COSStream.FilterFlags.RAW_DATA);
    }

    public ASInputStream getData(final COSStream.FilterFlags flags) {
        return getDirect().getData(flags);
    }

    public boolean setData(final ASInputStream stream) {
        return this.setData(stream, COSStream.FilterFlags.RAW_DATA);
    }

    public boolean setData(final ASInputStream stream, final COSStream.FilterFlags flags) {
        getDirect().setData(stream, flags);
        return true;
    }

    public Boolean isStreamKeywordCRLFCompliant() {
        return getDirect().isStreamKeywordCRLFCompliant();
    }

    public boolean setStreamKeywordCRLFCompliant(final boolean streamKeywordCRLFCompliant) {
        getDirect().setStreamKeywordCRLFCompliant(streamKeywordCRLFCompliant);
        return true;
    }

    public Boolean isEndstreamKeywordCRLFCompliant() {
        return getDirect().isEndstreamKeywordCRLFCompliant();
    }

    public boolean setEndstreamKeywordCRLFCompliant(final boolean endstreamKeywordCRLFCompliant) {
        getDirect().setEndstreamKeywordCRLFCompliant(endstreamKeywordCRLFCompliant);
        return true;
    }

    public Long getRealStreamSize() {
        return getDirect().getRealStreamSize();
    }

    public boolean setRealStreamSize(final long realStreamSize) {
        getDirect().setRealStreamSize(realStreamSize);
        return true;
    }

    //! Indirect object
    public Boolean isIndirect() {
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

    public COSObject getDirect() {
        return this.document != null ? this.document.getObject(key) : this.child;
    }

    public COSBase getDirectBase() {
        return this.document != null ? this.document.getObject(key).get() : this.child.get();
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
    public void mark() {
        if (this.document != null) {
            COSObject object = new COSObject(this);
            this.document.setObject(object);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSIndirect)) return false;

        COSIndirect that = (COSIndirect) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        return document == that.document;

    }
}
