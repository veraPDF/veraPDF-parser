/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASConcatenatedInputStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.InternalOutputStream;
import org.verapdf.io.SeekableInputStream;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
				return ASInputStream.createStreamFromStream(this.stream);
			}
			ASInputStream result = getFilters().getInputStream(
					ASInputStream.createStreamFromStream(stream),
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
			return setData(new InternalInputStream(encodedDataFile, true), FilterFlags.RAW_DATA);
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
		try (ASInputStream decoded = this.getData(COSStream.FilterFlags.DECODE);
			 SeekableInputStream unfilteredData =
					 SeekableInputStream.getSeekableStream(decoded)) {
			InternalOutputStream fileWithData = InternalOutputStream.getInternalOutputStream();
			setKey(ASAtom.FILTER, filters.getObject());
			ASOutputStream encoder = filters.getOutputStream(fileWithData);
			encoder.write(unfilteredData);
			File encodedDataFile = fileWithData.getFile();
			fileWithData.close();
			this.setData(new InternalInputStream(encodedDataFile, true), FilterFlags.RAW_DATA);
		}
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
		List<COSBasePair> checkedObjects = new LinkedList<>();
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

	public static COSObject concatenateStreams(COSArray streams) throws IOException {
		List<ASInputStream> resList = new ArrayList<>();
		for (COSObject stream : streams) {
			if (stream.getType() == COSObjType.COS_STREAM) {
				ASInputStream streamData = stream.getData(FilterFlags.DECODE);
				resList.add(streamData);
			}
		}
		ASInputStream[] asInputStreams = resList.toArray(new ASInputStream[resList.size()]);
		ASInputStream inputContentStream = new ASConcatenatedInputStream(asInputStreams);
		return COSStream.construct(inputContentStream);
	}
}
