package org.verapdf.cos;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Timur Kamalov
 */
public class COSStream extends COSDictionary {

	private static final Logger LOGGER = Logger.getLogger(COSStream.class);

	private ASInputStream stream;
	private FilterFlags flags;

	private boolean streamKeywordCRLFCompliant = true;
	private boolean endstreamKeywordCRLFCompliant = true;
	private long realStreamSize;

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

	protected COSStream(final COSDictionary dictionary) {
		super(dictionary);
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

	public static COSObject construct(final COSDictionary dictionary) {
		return new COSObject(new COSStream(dictionary));
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
		return COSObjType.COS_STREAM;
	}

	public void accept(final IVisitor visitor) {
		visitor.visitFromStream(this);
	}

	public Object accept(final ICOSVisitor visitor) {
		return visitor.visitFromStream(this);
	}

	public ASInputStream getData() {
		return getData(FilterFlags.RAW_DATA);
	}

	public ASInputStream getData(final FilterFlags flags) {
		try {
			if (flags == FilterFlags.RAW_DATA || this.flags != FilterFlags.RAW_DATA) {
				this.stream.reset();
				return this.stream;
			}
			ASInputStream result = getFilters().getInputStream(stream, this.getKey(ASAtom.DECODE_PARMS));
			result.reset();
			return result;
		} catch (IOException e) {
			LOGGER.error("Can't get stream data", e);
			return null;
		}
	}

	public boolean setData(final ASInputStream stream) {
		return setData(stream, FilterFlags.RAW_DATA);
	}

	public boolean setData(final ASInputStream stream, FilterFlags flags) {
		this.stream = stream;
		this.flags = flags;
		return true;
	}

	public Boolean isStreamKeywordCRLFCompliant() {
		return streamKeywordCRLFCompliant;
	}

	public boolean setStreamKeywordCRLFCompliant(boolean streamKeywordCRLFCompliant) {
		this.streamKeywordCRLFCompliant = streamKeywordCRLFCompliant;
		return true;
	}

	public Boolean isEndstreamKeywordCRLFCompliant() {
		return endstreamKeywordCRLFCompliant;
	}

	public boolean setEndstreamKeywordCRLFCompliant(boolean endstreamKeywordCRLFCompliant) {
		this.endstreamKeywordCRLFCompliant = endstreamKeywordCRLFCompliant;
		return true;
	}

	public Long getRealStreamSize() {
		return realStreamSize;
	}

	public boolean setRealStreamSize(long realStreamSize) {
		this.realStreamSize = realStreamSize;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof COSStream)) return false;
		if (!super.equals(o)) return false;

		COSStream cosStream = (COSStream) o;

		if (streamKeywordCRLFCompliant != cosStream.streamKeywordCRLFCompliant) return false;
		if (endstreamKeywordCRLFCompliant != cosStream.endstreamKeywordCRLFCompliant) return false;
		if (realStreamSize != cosStream.realStreamSize) return false;
		try {
			if (stream != null ? !equalsStreams(stream, cosStream.stream) : cosStream.stream != null) return false;
		} catch (IOException e) {
			LOGGER.debug("Exception during comparing streams");
			return false;
		}
		return flags == cosStream.flags;
	}

	private boolean equalsStreams(ASInputStream first, ASInputStream second) throws IOException {
		byte[] tempOne = new byte[1024];
		byte[] tempTwo = new byte[1024];
		int readFromOne;
		int readFromTwo;
		do {
			readFromOne = first.read(tempOne, tempOne.length);
			readFromTwo = second.read(tempTwo, tempTwo.length);
			if (readFromOne != readFromTwo || !Arrays.equals(tempOne, tempTwo)) {
				return false;
			}
		} while (readFromOne != 0);

		return true;
	}
}
