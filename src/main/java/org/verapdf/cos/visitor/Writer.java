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
package org.verapdf.cos.visitor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.verapdf.as.ASAtom;
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSBoolean;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSIndirect;
import org.verapdf.cos.COSInteger;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSName;
import org.verapdf.cos.COSNull;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSReal;
import org.verapdf.cos.COSStream;
import org.verapdf.cos.COSString;
import org.verapdf.cos.COSTrailer;
import org.verapdf.cos.xref.COSXRefEntry;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.cos.xref.COSXRefRange;
import org.verapdf.cos.xref.COSXRefSection;
import org.verapdf.io.InternalOutputStream;
import org.verapdf.io.SeekableInputStream;

/**
 * @author Timur Kamalov
 */
public class Writer implements IVisitor {

	private static final Logger LOGGER = Logger.getLogger(Writer.class.getCanonicalName());

	protected InternalOutputStream os;
	private final long incrementalOffset;
	protected COSXRefInfo info;

	protected COSDocument document;

	protected List<COSKey> toWrite;
	protected List<COSKey> written;

	private final NumberFormat formatXrefOffset = new DecimalFormat("0000000000");
	private final NumberFormat formatXrefGeneration = new DecimalFormat("00000");

	public static final String EOL = "\r\n";

	public Writer(final COSDocument document, final String filename,
				  final long incrementalOffset) throws IOException {
		this(document, filename, true, incrementalOffset);
	}

	public Writer(final COSDocument document, final String filename,
				  final boolean append, final long incrementalOffset) throws IOException {
		this.document = document;
		this.os = new InternalOutputStream(filename);
		this.info = new COSXRefInfo();

		this.toWrite = new ArrayList<>();
		this.written = new ArrayList<>();

		this.incrementalOffset = incrementalOffset;

		if (append) {
			this.os.seekEnd();
		}
	}

	public void writeIncrementalUpdate(final List<COSObject> changedObjects,
									   final List<COSObject> addedObjects) {
		final List<COSKey> objectsToWrite = new ArrayList<>();
		for (final COSObject obj : changedObjects) {
			final COSKey key = obj.getObjectKey();
			if (key != null) {
				objectsToWrite.add(obj.getObjectKey());
			}
		}
		changedObjects.clear();
		objectsToWrite.addAll(this.prepareAddedObjects(addedObjects));
		this.addToWrite(objectsToWrite);
		this.writeBody();
		final COSTrailer trailer = this.document.getTrailer();

		// document.getLastTrailerOffset() + 1 point EXACTLY at first byte of xref
		this.setTrailer(trailer, this.document.getLastTrailerOffset() + 1);
		this.writeXRefInfo();
		this.clear();
	}

	private List<COSKey> prepareAddedObjects(final List<COSObject> addedObjects) {
		final int cosKeyNumber = this.document.getLastKeyNumber() + 1;
		final List<COSKey> res = new ArrayList<>();
		for (final COSObject obj : addedObjects) {
			if (!obj.isIndirect()) {
				final COSObject indirect = COSIndirect.construct(obj, this.document);
				res.add(indirect.getObjectKey());
			} else {
				res.add(obj.getObjectKey());
			}
		}
		addedObjects.clear();
		return res;
	}

	@Override
    public void visitFromBoolean(final COSBoolean obj) {
		try {
			this.write(String.valueOf(obj.get()));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void visitFromInteger(final COSInteger obj) {
		try {
			this.write(obj.toString());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void visitFromReal(final COSReal obj) {
		try {
			this.write(obj.toString());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void visitFromString(final COSString obj) {
		try {
			this.write(obj.getPrintableString());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void visitFromName(final COSName obj) {
		try {
			this.write(obj.toString());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void visitFromArray(final COSArray obj) {
		try {
			this.write("[");
			for (int i = 0; i < obj.size(); i++) {
				this.write(obj.at(i));
				this.write(" ");
			}
			this.write("]");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void visitFromDictionary(final COSDictionary obj) {
		try {
			this.write("<<");
			for (final Map.Entry<ASAtom, COSObject> entry : obj.getEntrySet()) {
				this.write(entry.getKey());
				this.write(" ");
				this.write(entry.getValue());
				this.write(" ");
			}
			this.write(">>");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void visitFromStream(final COSStream obj) {
		long length;

		final ASInputStream in = obj.getData();

		if (obj.getFilterFlags() == COSStream.FilterFlags.DECODE ||
				obj.getFilterFlags() == COSStream.FilterFlags.DECRYPT_AND_DECODE) {
			//TODO : Decode
		}
		try {
			obj.setIntegerKey(ASAtom.LENGTH, getASInputStreamLength(in));
		} catch (final IOException e) {
			LOGGER.log(Level.FINE, "Can't calculate length of ASInputStream", e);
		}

		this.visitFromDictionary(obj);

		try {
			this.write(EOL);
			this.write("stream");
			this.write(EOL);

			length = this.getOffset();

			final byte[] buffer = new byte[1024];
			long count;

			in.reset();

			while(true) {
				count = in.read(buffer, 1024);
				if (count == -1) {
					break;
				}
				this.os.write(buffer, (int) count);
			}

			length = this.getOffset() - length;
			obj.setLength(length);

			this.write(EOL);
			this.write("endstream");
		} catch (final IOException e) {
			throw new RuntimeException(StringExceptions.WRITE_ERROR);
		}
	}

	private static long getASInputStreamLength(final ASInputStream stream) throws IOException {
		if (stream instanceof SeekableInputStream) {
			// That is the case of unfiltered stream
			return ((SeekableInputStream) stream).getStreamLength();
		} else {
			// That is the case of fitered stream. Optimization can be reached
			// if decoded data is stored in memory and not thrown away.
			stream.reset();
			final byte[] buf = new byte[ASBufferedInFilter.BF_BUFFER_SIZE];
			long res = 0;
			int read = stream.read(buf);
			while (read != -1) {
				res += read;
				read = stream.read(buf);
			}
			return res;
		}
	}

	@Override
    public void visitFromNull(final COSNull obj) {
		try {
			this.write("null");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
    public void visitFromIndirect(final COSIndirect obj) {
		try {
			COSKey key = obj.getKey();

			if (key.equals(new COSKey())) {
				final COSObject direct = obj.getDirect();
				key = this.document.setObject(direct);
				obj.setKey(key, this.document);
				this.addToWrite(key);
			}

			this.write(key);
			this.write(" R");
		} catch (final IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void writeHeader(final String header) {
		try {
			this.write(header);
			this.write(EOL);

			final String comment = new String(new char[] { '%', 0xE2, 0xE3, 0xCF, 0xD3 });
			this.write(comment);
			this.write(EOL);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void addToWrite(final COSKey key) {
		this.toWrite.add(key);
	}

	public void addToWrite(final List<COSKey> keys) {
		this.toWrite.addAll(keys);
	}

	public void writeBody() {
		try {
			while (!this.toWrite.isEmpty()) {
				final COSKey key = this.toWrite.get(0);

				this.toWrite.remove(0);
				this.written.add(key);

				this.write(key, this.document.getObject(key));
			}
		} catch (final IOException e) {
			throw new RuntimeException(StringExceptions.WRITE_ERROR);
		}
	}

	public void freeObjects(final Map<COSKey, Long> keys) {
		for (final Map.Entry<COSKey, Long> entry : keys.entrySet()) {
			this.addXRef(entry.getKey(), entry.getValue(), 'f');
		}
	}

	public void setTrailer(final COSTrailer trailer) {
		this.setTrailer(trailer, 0);
	}

	public void setTrailer(final COSTrailer trailer, final long prev) {
		final COSObject element = new COSObject();
		final COSCopier copier = new COSCopier(element);
		trailer.getObject().accept(copier);

		this.info.getTrailer().setObject(element);

		this.info.getTrailer().setPrev(prev);

		if (prev == 0) {
			this.info.getTrailer().removeKey(ASAtom.ID);
		}
	}

	public void writeXRefInfo() {
		try {
			this.info.setStartXRef(this.getOffset() + this.incrementalOffset);

			this.info.getTrailer().setSize(this.info.getXRefSection().next());

			this.write("xref"); this.write(EOL); this.write(this.info.getXRefSection());
			this.write("trailer"); this.write(EOL); this.write(this.info.getTrailer().getObject()); this.write(EOL);
			this.write("startxref"); this.write(EOL); this.write(this.info.getStartXRef()); this.write(EOL);
			this.write("%%EOF"); this.write(EOL);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public COSXRefInfo getXRefInfo() {
		return this.info;
	}

	public void clear() {
		try {
			this.info = new COSXRefInfo();

			this.toWrite.clear();
			this.written.clear();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.os.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected long getOffset() {
		try {
			return this.os.getOffset();
		} catch (final IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	protected void write(final COSKey key, final COSObject object) throws IOException {
		this.addXRef(key);
		this.write(key);
		this.write(" obj");
		this.write(EOL);
		this.write(object);
		this.write(EOL);
		this.write("endobj");
		this.write(EOL);
	}

	protected void generateID() {
		// TODO : finish this method
		final Long idTime = System.currentTimeMillis();
		MessageDigest md5;
		try	{
			md5 = MessageDigest.getInstance("MD5");
			md5.update(Long.toString(idTime).getBytes(StandardCharsets.ISO_8859_1));
			final COSObject idString = COSString.construct(md5.digest(), true);
			//TODO : convert to COSArray
			this.info.getTrailer().setID(idString);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	protected COSKey getKeyToWrite(final COSKey key) {
		return key;
	}

	protected void addXRef(final COSKey key, final long offset, final char free) {
		this.info.getXRefSection().add(this.getKeyToWrite(key), offset, free);
	}

	public void addXRef(final COSKey key) {
		this.addXRef(key, this.getOffset() + this.incrementalOffset, 'n');
	}

	protected void write(final boolean value) throws IOException {
		this.os.write(value);
	}

	protected void write(final int value) throws IOException {
		this.os.write(String.valueOf(value));
	}

	protected void write(final long value) throws IOException {
		this.os.write(String.valueOf(value));
	}

	protected void write(final char value) throws IOException {
		this.os.write(value);
	}

	protected void write(final String value) throws IOException {
		this.os.write(value);
	}

	protected void write(final ASAtom value) throws IOException {
		this.os.write(value.toString());
	}

	protected void write(final COSKey value) throws IOException {
		final COSKey newKey = this.getKeyToWrite(value);
		this.write(newKey.getNumber()); this.write(" "); this.write(newKey.getGeneration());
	}

	protected void write(final COSObject value) {
		value.accept(this);
	}

	protected void write(final COSXRefRange value) throws IOException {
		this.os.write(String.valueOf(value.start)).write(" ").write(String.valueOf(value.count)).write(EOL);
	}

	protected void write(final COSXRefEntry value) throws IOException {
		final String offset = this.formatXrefOffset.format(value.offset);
		final String generation = this.formatXrefGeneration.format(value.generation);
		this.os.write(offset.getBytes(StandardCharsets.ISO_8859_1));
		this.os.write(" ");
		this.os.write(generation.getBytes(StandardCharsets.ISO_8859_1));
		this.os.write(" ");
		this.os.write(String.valueOf(value.free).getBytes(StandardCharsets.US_ASCII));
		this.os.write(EOL);
	}

	protected void write(final COSXRefSection value) throws IOException {
		final List<COSXRefRange> range = value.getRange();
		for (final COSXRefRange entry : range) {
			this.write(entry);
			for (int j = entry.start; j < entry.next(); j++) {
				this.write(value.getEntry(j));
			}
		}
	}

}
