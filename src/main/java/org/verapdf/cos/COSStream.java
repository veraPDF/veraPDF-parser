package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.visitor.IVisitor;

/**
 * @author Timur Kamalov
 */
public class COSStream extends COSDictionary {

	private ASInputStream stream;
	private FilterFlags flags;

	protected COSStream() {
		super();
		this.flags = FilterFlags.RAW_DATA;

		setIndirectLength(0);
	}

	protected COSStream(final ASInputStream stream) {
		super();
		this.stream = stream;
		this.flags = FilterFlags.RAW_DATA;

		setIndirectLength(0);
	}

	protected COSStream(final String string) {
		super();
		//TODO : Memory in stream
		this.flags = FilterFlags.RAW_DATA;
	}

	protected COSStream(final COSDictionary dictionary, final ASInputStream stream, final FilterFlags flags) {
		super(dictionary);
		this.stream = stream;
		this.flags = flags;
	}

	protected COSStream(final COSDictionary dictionary, final String string, final FilterFlags flags) {
		super(dictionary);
		//TODO : Memory in stream
		this.flags = flags;
	}

	public static COSObject construct() {
		return new COSObject(new COSStream());
	}

	public static COSObject construct(final ASInputStream stream) {
		return new COSObject(new COSStream(stream));
	}

	public static COSObject construct(final String string) {
		return new COSObject(new COSStream(string));
	}

	public static COSObject construct(final COSDictionary dictionary, final ASInputStream stream) {
		return construct(dictionary, stream, FilterFlags.RAW_DATA);
	}

	public static COSObject construct(final COSDictionary dictionary, final ASInputStream stream, final FilterFlags flags) {
		return new COSObject(new COSStream(dictionary, stream, flags));
	}

	public static COSObject construct(final COSDictionary dictionary, final String string) {
		return construct(dictionary, string, FilterFlags.RAW_DATA);
	}

	public static COSObject construct(final COSDictionary dictionary, final String string, final FilterFlags flags) {
		return new COSObject(new COSStream(dictionary, string, flags));
	}

	public COSObjType getType() {
		return COSObjType.COSStreamT;
	}

	public void accept(final IVisitor visitor) {
		visitor.visitFromStream(this);
	}

	public ASInputStream getData() {
		return getData(FilterFlags.RAW_DATA);
	}

	public ASInputStream getData(final FilterFlags flags) {
		if (flags == FilterFlags.RAW_DATA || this.flags != FilterFlags.RAW_DATA) {
			return this.stream;
		}

		return getFilters().getInputStream(stream);
	}

	public boolean setData(final ASInputStream stream) {
		return setData(stream, FilterFlags.RAW_DATA);
	}

	public boolean setData(final ASInputStream stream, FilterFlags flags) {
		this.stream = stream;
		this.flags = flags;
		return true;
	}

	public COSFilters getFilters() {
		return new COSFilters(getKey(ASAtom.FILTER));
	}

	public void setFilters(final COSFilters filters) {
		setKey(ASAtom.FILTER, filters.getObject());
	}

	public FilterFlags getFilterFlags() {
		return this.flags;
	}

	public void setFilterFlags(final FilterFlags flags) {
		this.flags = flags;
	}

	public long getLength() {
		return getIntegerKey(ASAtom.LENGTH);
	}

	public void setLength(final long length) {
		setIntegerKey(ASAtom.LENGTH, length);
	}

	public void setIndirectLength(final long length) {
		COSObject obj = getKey(ASAtom.LENGTH);
		obj.setInteger(length);
		if (obj.isIndirect()) {
			obj = COSIndirect.construct(obj);
			setKey(ASAtom.LENGTH, obj);
		}
	}


	public enum FilterFlags {
		RAW_DATA,
		DECODE,
		DECRYPT,
		DECRYPT_AND_DECODE
	}

}
