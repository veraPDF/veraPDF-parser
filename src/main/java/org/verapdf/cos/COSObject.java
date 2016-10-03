package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.visitor.IVisitor;

import java.util.Collection;
import java.util.Set;

/**
 * @author Timur Kamalov
 */
public class COSObject {

	private final static COSObject empty = new COSObject();

	private COSBase base;

	//pdf/a validation specific things
	private boolean isHeaderOfObjectComplyPDFA = true;
	private boolean isEndOfObjectComplyPDFA = true;
	private boolean isHeaderFormatComplyPDFA = true;

	public COSObject() {
	}

	public COSObject(final COSBase base) {
		set(base);
	}

	public COSObject(final COSObject object) {
		set(object.base);
	}

	// Access to base underlying object
	public COSBase get() {
		return this.base;
	}

	public void set(COSBase base) {
		if (this.base == base) {
			return;
		}

		this.base = base;
	}

	public void assign(COSObject object) {
		if (object == null && object.get() == null) {
			return;
		}

		if (this.base != object.get()) {
			set(object.get());
		}
	}

	public boolean empty() {
		return base == null;
	}

	public void clear() {
		this.set(null);
	}

	public COSKey getObjectKey() {
		return this.base != null ? this.base.getObjectKey() : null;
	}

	public void setObjectKey(final COSKey key) {
		if (this.base != null) {
			this.base.setObjectKey(key);
		}
	}

	// OBJECT TYPE
	public COSObjType getType() {
		return this.base != null ? this.base.getType() : COSObjType.COS_UNDEFINED;
	}

	// VISITOR DESIGN PATTERN
	public void accept(IVisitor visitor) {
		if (this.base != null) {
			this.base.accept(visitor);
		}
	}

	// BOOLEAN VALUES
	public Boolean getBoolean() {
		return this.base != null ? this.base.getBoolean() : null;
	}

	public void setBoolean(final boolean value) {
		if(this.base == null || !this.base.setBoolean(value)) {
			this.base = new COSBoolean(value);
		}
	}

	// INTEGER NUMBERS
	public Long getInteger() {
		return this.base != null ? this.base.getInteger() : null;
	}

	public void setInteger(final long value) {
		if (this.base == null || !this.base.setInteger(value)) {
			this.base = new COSInteger(value);
		}
	}

	//! Real numbers
	public Double getReal() {
		return this.base != null ? this.base.getReal() : null;
	}

	public void setReal(final double value) {
		if (this.base == null || !this.base.setReal(value)) {
			this.base = new COSReal(value);
		}
	}

	//! Strings
	public String getString() {
		return this.base != null ? this.base.getString() : null;
	}

	public void setString(final String value) {
		setString(value, false);
	}

	public void setString(final String value, final boolean isHex) {
		if (this.base == null || !this.base.setString(value, isHex)) {
			this.base = new COSString(value, isHex);
		}
	}

	//! Names
	public ASAtom getName() {
		return this.base != null ? this.base.getName() : null;
	}

	public void setName(final ASAtom value) {
		if (this.base == null || !this.base.setName(value)) {
			this.base = new COSName(value);
		}
	}

	//! Number of elements in array and dictionary
	public Integer size() {
		return this.base != null ? this.base.size() : null;
	}

	public COSObject at(final int i) {
		return this.base != null ? this.base.at(i) : this;
	}

	public void add(final COSObject value) {
		if (this.base == null || !this.base.add(value)) {
			this.base = new COSArray(1, value);
		}
	}

	public void set(final int i, final COSObject value) {
		if (this.base == null || !this.base.set(i, value)) {
			this.base = new COSArray(i, value);
		}
	}

	public void insert(final int i, final COSObject value) {
		if (this.base == null || !this.base.insert(i, value)) {
			this.base = new COSArray(i, value);
		}
	}

	public void remove(final int i) {
		if (this.base != null) {
			this.base.remove(i);
		}
	}

	public void setArray() {
		if (this.base == null || !this.base.setArray()) {
			this.base = new COSArray();
		}
	}

	public void setArray(final int size, final COSObject[] value) {
		if (this.base == null || !this.base.setArray(size, value)) {
			this.base = new COSArray(size, value);
		}
	}

	public void setArray(final int size, final double[] value) {
		if (this.base == null || !this.base.setArray(size, value)) {
			this.base = new COSArray(size, value);
		}
	}

	public void clearArray() {
		if (this.base != null) {
			this.base.clearArray();
		}
	}

	//! Dictionaries
	public Boolean knownKey(final ASAtom key) {
		return this.base != null ? this.base.knownKey(key) : null;
	}

	public COSObject getKey(final ASAtom key) {
		return this.base != null ? this.base.getKey(key) : null;
	}

	public void setKey(final ASAtom key, final COSObject value) {
		if (this.base == null || !this.base.setKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public Boolean getBooleanKey(final ASAtom key) {
		return this.base != null ? this.base.getBooleanKey(key) : null;
	}

	public void setBooleanKey(final ASAtom key, final boolean value) {
		if (this.base == null || !this.base.setBooleanKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public Long getIntegerKey(final ASAtom key) {
		return this.base != null ? this.base.getIntegerKey(key) : null;
	}

	public void setIntegerKey(final ASAtom key, final long value) {
		if (this.base == null || !this.base.setIntegerKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public Double getRealKey(final ASAtom key) {
		return this.base != null ? this.base.getRealKey(key) : null;
	}

	public void setRealKey(final ASAtom key, final double value) {
		if (this.base == null || !this.base.setRealKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public String getStringKey(final ASAtom key) {
		return this.base != null ? this.base.getStringKey(key) : null;
	}

	public void setStringKey(final ASAtom key, final String value) {
		if (this.base == null || !this.base.setStringKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public ASAtom getNameKey(final ASAtom key) {
		return this.base != null ? this.base.getNameKey(key) : null;
	}

	public void setNameKey(final ASAtom key, final ASAtom value) {
		if (this.base == null || !this.base.setNameKey(key, value)) {
			this.base = new COSDictionary(key, value);
		}
	}

	public void setArrayKey(final ASAtom key) {
		if (this.base == null || !this.base.setArrayKey(key)) {
			COSObject obj = COSArray.construct();
			this.base = new COSDictionary(key, obj);
		}
	}

	public void setArrayKey(final ASAtom key, final COSObject array) {
		if (this.base == null || !this.base.setArrayKey(key, array)) {
			COSObject obj = COSArray.construct();
			this.base = new COSDictionary(key, obj);
		}
	}

	public void setArrayKey(final ASAtom key, final int size, final COSObject[] value) {
		if (this.base == null || !this.base.setArrayKey(key, size, value)) {
			COSObject obj = COSArray.construct(size, value);
			this.base = new COSDictionary(key, obj);
		}
	}

	public void setArrayKey(final ASAtom key, final int size, final double[] value) {
		if (this.base == null || this.base.setArrayKey(key, size, value)) {
			COSObject obj = COSArray.construct(size, value);
			this.base = new COSDictionary(key, obj);
		}
	}

	public void removeKey(final ASAtom key) {
		if (this.base != null) {
			this.base.removeKey(key);
		}
	}

	public Set<ASAtom> getKeySet() {
		if (this.base != null) {
			return this.base.getKeySet();
		}
		return null;
	}

	public Collection<COSObject> getValues() {
		if (this.base != null) {
			return this.base.getValues();
		}
		return null;
	}


	public ASInputStream getData() {
		return getData(COSStream.FilterFlags.RAW_DATA);
	}

	public ASInputStream getData(final COSStream.FilterFlags flags) {
		return this.base != null ? this.base.getData(flags) : null;
	}

	public void setData(final ASInputStream stream) {
		setData(stream, COSStream.FilterFlags.RAW_DATA);
	}

	public void setData(final ASInputStream stream, final COSStream.FilterFlags flags) {
		if (this.base == null || !this.base.setData(stream, flags)) {
			COSObject obj;
			if (this.base instanceof COSDictionary) {
				obj = COSStream.construct((COSDictionary) this.base, stream, flags);
			} else {
				obj = COSStream.construct(stream);
			}
			this.base = obj.base;
		}
	}

	public Boolean isStreamKeywordCRLFCompliant() {
		return this.base != null ? this.base.isStreamKeywordCRLFCompliant() : null;
	}

	public void setStreamKeywordCRLFCompliant(final boolean streamKeywordCRLFCompliant) {
		if (this.base == null || !this.base.setStreamKeywordCRLFCompliant(streamKeywordCRLFCompliant)) {
			COSObject obj;
			if (this.base instanceof COSDictionary) {
				obj = COSStream.construct((COSDictionary) this.base);
			} else {
				obj = COSStream.construct();
			}
			this.base = obj.base;
			this.base.setStreamKeywordCRLFCompliant(streamKeywordCRLFCompliant);
		}
	}

	public Boolean isEndstreamKeywordCRLFCompliant() {
		return this.base != null ? this.base.isEndstreamKeywordCRLFCompliant() : null;
	}

	public void setEndstreamKeywordCRLFCompliant(final boolean endstreamKeywordCRLFCompliant) {
		if (this.base == null || !this.base.setEndstreamKeywordCRLFCompliant(endstreamKeywordCRLFCompliant)) {
			COSObject obj;
			if (this.base instanceof COSDictionary) {
				obj = COSStream.construct((COSDictionary) this.base);
			} else {
				obj = COSStream.construct();
			}
			this.base = obj.base;
			this.base.setEndstreamKeywordCRLFCompliant(endstreamKeywordCRLFCompliant);
		}
	}

	public Long getRealStreamSize() {
		return this.base != null ? this.base.getRealStreamSize() : null;
	}

	public void setRealStreamSize(final long realStreamSize) {
		if (this.base == null || !this.base.setRealStreamSize(realStreamSize)) {
			COSObject obj;
			if (this.base instanceof COSDictionary) {
				obj = COSStream.construct((COSDictionary) this.base);
			} else {
				obj = COSStream.construct();
			}
			this.base = obj.base;
			this.base.setRealStreamSize(realStreamSize);
		}
	}

	//! Indirect object
	public Boolean isIndirect() {
		return this.base != null ? this.base.isIndirect() : null;
	}

	public COSKey getKey() {
		return this.base != null ? this.base.getKey() : null;
	}

	public COSDocument getDocument() {
		return this.base != null ? this.base.getDocument() : null;
	}

	public void setKey(final COSKey key, final COSDocument document) {
		if (this.base == null || this.base.setKey(key, document)) {
			this.base = new COSIndirect(key, document);
		}
	}

	public COSObject getDirect() {
		return this.base != null ? this.base.getDirect() : null;
	}

	public COSBase getDirectBase() {
		return this.base != null ? this.base.getDirectBase() : null;
	}

	public void setDirect(final COSObject value) {
		if (this.base == null || !this.base.setDirect(value)) {
			set(value.base);
		}
	}

	//! Marks object for incremental update.
	//! (If object is indirect and its document is known.)
	public void mark() {
		if (this.base != null) {
			this.base.mark();
		}
	}

	public static COSObject getEmpty() {
		return empty;
	}

	//GETTERS & SETTERS
	public Boolean isHeaderOfObjectComplyPDFA() {
		return this.isHeaderOfObjectComplyPDFA;
	}

	public void setIsHeaderOfObjectComplyPDFA(Boolean isHeaderOfObjectComplyPDFA) {
		this.isHeaderOfObjectComplyPDFA = isHeaderOfObjectComplyPDFA;
	}

	public Boolean isEndOfObjectComplyPDFA() {
		return this.isEndOfObjectComplyPDFA;
	}

	public void setIsEndOfObjectComplyPDFA(Boolean isEndOfObjectComplyPDFA) {
		this.isEndOfObjectComplyPDFA = isEndOfObjectComplyPDFA;
	}

	public Boolean isHeaderFormatComplyPDFA() {
		return this.isHeaderFormatComplyPDFA;
	}

	public void setIsHeaderFormatComplyPDFA(Boolean isHeaderFormatComplyPDFA) {
		this.isHeaderFormatComplyPDFA = isHeaderFormatComplyPDFA;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof COSObject)) return false;

		COSObject cosObject = (COSObject) o;

		if (isHeaderOfObjectComplyPDFA != cosObject.isHeaderOfObjectComplyPDFA) return false;
		if (isEndOfObjectComplyPDFA != cosObject.isEndOfObjectComplyPDFA) return false;
		if (isHeaderFormatComplyPDFA != cosObject.isHeaderFormatComplyPDFA) return false;
		return base != null ? base.equals(cosObject.base) : cosObject.base == null;

	}
}
