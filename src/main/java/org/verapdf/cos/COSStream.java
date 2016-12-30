package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.InternalOutputStream;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.tools.resource.ClosableASInputStreamWrapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class COSStream extends COSDictionary {

	private static final Logger LOGGER = Logger.getLogger(COSStream.class.getCanonicalName());

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
		this.stream = new ASMemoryInStream(string.getBytes());
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
		this.stream = new ASMemoryInStream(string.getBytes());
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

	@Override
	public COSObjType getType() {
		return COSObjType.COS_STREAM;
	}

	@Override
	public void accept(final IVisitor visitor) {
		visitor.visitFromStream(this);
	}

	@Override
	public Object accept(final ICOSVisitor visitor) {
		return visitor.visitFromStream(this);
	}

	@Override
	public ASInputStream getData() {
		return getData(FilterFlags.RAW_DATA);
	}

	@Override
	public ASInputStream getData(final FilterFlags filterFlags) {
		try {
			if (filterFlags == FilterFlags.RAW_DATA || this.flags != FilterFlags.RAW_DATA) {
				this.stream.reset();
				return this.stream;
			}
			ASInputStream result = getFilters().getInputStream(
					new ClosableASInputStreamWrapper(stream),
					this.getKey(ASAtom.DECODE_PARMS));
			result.reset();
			return result;
		} catch (IOException e) {
			LOGGER.log(Level.FINE, "Can't get stream data", e);
			return null;
		}
	}

	@Override
	public boolean setData(final ASInputStream stream) {
		COSFilters filters = getFilters();
		if (filters.empty()) {
			return setData(stream, FilterFlags.RAW_DATA);
		}
		try (InternalOutputStream fileWithData = InternalOutputStream.getInternalOutputStream()) {
			ASOutputStream encoder = filters.getOutputStream(fileWithData);
			encoder.write(stream);
			File encodedDataFile = fileWithData.getFile();
			return setData(new InternalInputStream(encodedDataFile), FilterFlags.RAW_DATA);
		} catch (IOException e) {
			LOGGER.log(Level.FINE, "Can not set data", e);
			return false;
		}
	}

	@Override
	public boolean setData(final ASInputStream stream, FilterFlags flags) {
		this.stream = stream;
		this.flags = flags;
		return true;
	}

	@Override
	public Boolean isStreamKeywordCRLFCompliant() {
		return Boolean.valueOf(streamKeywordCRLFCompliant);
	}

	@Override
	public boolean setStreamKeywordCRLFCompliant(boolean streamKeywordCRLFCompliant) {
		this.streamKeywordCRLFCompliant = streamKeywordCRLFCompliant;
		return true;
	}

	@Override
	public Boolean isEndstreamKeywordCRLFCompliant() {
		return Boolean.valueOf(endstreamKeywordCRLFCompliant);
	}

	@Override
	public boolean setEndstreamKeywordCRLFCompliant(boolean endstreamKeywordCRLFCompliant) {
		this.endstreamKeywordCRLFCompliant = endstreamKeywordCRLFCompliant;
		return true;
	}

	@Override
	public Long getRealStreamSize() {
		return Long.valueOf(realStreamSize);
	}

	@Override
	public boolean setRealStreamSize(long realStreamSize) {
		this.realStreamSize = realStreamSize;
		return true;
	}

	public COSFilters getFilters() {
		return new COSFilters(getKey(ASAtom.FILTER));
	}

	public void setFilters(final COSFilters filters) throws IOException {
		SeekableInputStream unfilteredData =
				SeekableInputStream.getSeekableStream(this.getData(COSStream.FilterFlags.DECODE));
		InternalOutputStream fileWithData = InternalOutputStream.getInternalOutputStream();
		setKey(ASAtom.FILTER, filters.getObject());
		ASOutputStream encoder = filters.getOutputStream(fileWithData);
		encoder.write(unfilteredData);
		File encodedDataFile = fileWithData.getFile();
		fileWithData.close();
		this.setData(new InternalInputStream(encodedDataFile), FilterFlags.RAW_DATA);
	}

	public FilterFlags getFilterFlags() {
		return this.flags;
	}

	public void setFilterFlags(final FilterFlags flags) {
		this.flags = flags;
	}

	public long getLength() {
		return getIntegerKey(ASAtom.LENGTH).longValue();
	}

    public void setLength(final long length) {
		setIntegerKey(ASAtom.LENGTH, length);
	}

	public void setIndirectLength(final long length) {
		COSObject obj = getKey(ASAtom.LENGTH);
		obj.setInteger(length);
		if (obj.isIndirect().booleanValue()) {
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
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(obj instanceof COSObject) {
			return this.equals(((COSObject) obj).get());
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
		COSStream that = (COSStream) obj;

		for(Map.Entry<ASAtom, COSObject> entry : this.getEntrySet()) {
			if(entry.getKey() == ASAtom.FILTER ||
					entry.getKey() == ASAtom.DECODE_PARMS ||
					entry.getKey() == ASAtom.LENGTH) {
				continue;
			}
			COSBase cosBase = that.getKey(entry.getKey()).get();
			if(!entry.getValue().get().equals(cosBase, checkedObjects)) {
				return false;
			}
		}

		for(Map.Entry<ASAtom, COSObject> entry : that.getEntrySet()) {
			if(entry.getKey() == ASAtom.FILTER ||
					entry.getKey() == ASAtom.DECODE_PARMS ||
					entry.getKey() == ASAtom.LENGTH) {
				continue;
			}
			COSBase cosBase = this.getKey(entry.getKey()).get();
			if(!entry.getValue().get().equals(cosBase, checkedObjects)) {
				return false;
			}
		}

		try {
			if (stream != null ? !equalsStreams(stream, that.stream) :
					that.stream != null) return false;
		} catch (IOException e) {
			LOGGER.log(Level.FINE, "Exception during comparing streams", e);
			return false;
		}
		return flags == that.flags;
	}

	private static boolean equalsStreams(ASInputStream first, ASInputStream second) throws IOException {
		first.reset();
		second.reset();
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
		} while (readFromOne != -1);

		return true;
	}
}
