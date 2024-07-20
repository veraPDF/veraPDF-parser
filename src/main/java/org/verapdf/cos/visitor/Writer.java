/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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

import org.verapdf.as.ASAtom;
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.as.filters.io.ASBufferedInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;
import org.verapdf.cos.xref.COSXRefEntry;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.cos.xref.COSXRefRange;
import org.verapdf.cos.xref.COSXRefSection;
import org.verapdf.exceptions.VeraPDFParserException;
import org.verapdf.io.InternalOutputStream;
import org.verapdf.io.SeekableInputStream;

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

/**
 * @author Timur Kamalov
 */
public class Writer implements IVisitor {

	private static final Logger LOGGER = Logger.getLogger(Writer.class.getCanonicalName());

	protected InternalOutputStream os;
	private final long incrementalOffset;
	protected COSXRefInfo info;

	protected final COSDocument document;

	protected List<COSKey> toWrite;
	protected List<COSKey> written;

	private final NumberFormat formatXrefOffset = new DecimalFormat("0000000000");
	private final NumberFormat formatXrefGeneration = new DecimalFormat("00000");

	public static final String EOL = "\r\n";

	public Writer(final COSDocument document, final String filename,
				  long incrementalOffset) throws IOException {
		this(document, filename, true, incrementalOffset);
	}

	public Writer(final COSDocument document, final String filename,
				  final boolean append, long incrementalOffset) throws IOException {
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

	public void writeIncrementalUpdate(List<COSObject> changedObjects,
									   List<COSObject> addedObjects) {
		List<COSKey> objectsToWrite = new ArrayList<>();
		for (COSObject obj : changedObjects) {
			COSKey key = obj.getObjectKey();
			if (key != null) {
				objectsToWrite.add(obj.getObjectKey());
			}
		}
		changedObjects.clear();
		objectsToWrite.addAll(prepareAddedObjects(addedObjects));
		this.addToWrite(objectsToWrite);
		this.writeBody();
		COSTrailer trailer = document.getTrailer();

		// document.getLastTrailerOffset() + 1 point EXACTLY at first byte of xref
		this.setTrailer(trailer, document.getLastTrailerOffset() + 1);
		this.writeXRefInfo();
		this.clear();
	}

	private List<COSKey> prepareAddedObjects(List<COSObject> addedObjects) {
		List<COSKey> res = new ArrayList<>();
		for (COSObject obj : addedObjects) {
            if (obj.isIndirect()) {
                res.add(obj.getObjectKey());
            } else {
                COSObject indirect = COSIndirect.construct(obj, this.document);
                res.add(indirect.getObjectKey());
            }
		}
		addedObjects.clear();
		return res;
	}

	@Override
	public void visitFromBoolean(COSBoolean obj) {
		try {
			this.write(String.valueOf(obj.get()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visitFromInteger(COSInteger obj) {
		try {
			this.write(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visitFromReal(COSReal obj) {
		try {
			this.write(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visitFromString(COSString obj) {
		try {
			this.write(obj.getPrintableString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visitFromName(COSName obj) {
		try {
			this.write(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visitFromArray(COSArray obj) {
		try {
			this.write("[");
			for (int i = 0; i < obj.size(); i++) {
				this.write(obj.at(i));
				this.write(" ");
			}
			this.write("]");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visitFromDictionary(COSDictionary obj) {
		try {
			this.write("<<");
			for (Map.Entry<ASAtom, COSObject> entry : obj.getEntrySet()) {
				this.write(entry.getKey());
				this.write(" ");
				this.write(entry.getValue());
				this.write(" ");
			}
			this.write(">>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visitFromStream(COSStream obj) {

		ASInputStream in = obj.getData();

		if (obj.getFilterFlags() == COSStream.FilterFlags.DECODE ||
				obj.getFilterFlags() == COSStream.FilterFlags.DECRYPT_AND_DECODE) {
			//TODO : Decode
		}
		try {
			obj.setIntegerKey(ASAtom.LENGTH, getASInputStreamLength(in));
		} catch (IOException e) {
			LOGGER.log(Level.FINE, "Can't calculate length of ASInputStream", e);
		}

		visitFromDictionary(obj);

		try {
			this.write(EOL);
			this.write("stream");
			this.write(EOL);

			long length = getOffset();

			in.reset();

			byte[] buffer = new byte[1024];
			while(true) {
				long count = in.read(buffer, 1024);
				if (count == -1) {
					break;
				}
				this.os.write(buffer, (int) count);
			}

			length = getOffset() - length;
			obj.setLength(length);

			this.write(EOL);
			this.write("endstream");
		} catch (IOException e) {
			throw new VeraPDFParserException(StringExceptions.WRITE_ERROR);
		}
	}

	private static long getASInputStreamLength(ASInputStream stream) throws IOException {
		if (stream instanceof SeekableInputStream) {
			// That is the case of unfiltered stream
			return ((SeekableInputStream) stream).getStreamLength();
		} else {
			// That is the case of filtered stream. Optimization can be reached
			// if decoded data is stored in memory and not thrown away.
			stream.reset();
			byte[] buf = new byte[ASBufferedInFilter.BF_BUFFER_SIZE];
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
	public void visitFromNull(COSNull obj) {
		try {
			this.write("null");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visitFromIndirect(COSIndirect obj) {
		try {
			COSKey key = obj.getKey();

			if (key.equals(new COSKey())) {
				COSObject direct = obj.getDirect();
				key = this.document.setObject(direct);
				obj.setKey(key, this.document);
				addToWrite(key);
			}

			this.write(key);
			this.write(" R");
		} catch (IOException e) {
			throw new VeraPDFParserException(e.getMessage());
		}
	}

	public void writeHeader(final String header) {
		try {
			this.write(header);
			this.write(EOL);

			String comment = new String(new char[] { '%', 0xE2, 0xE3, 0xCF, 0xD3 });
			this.write(comment);
			this.write(EOL);
		} catch (IOException e) {
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
			this.write("\r\n");
			while (!this.toWrite.isEmpty()) {
				final COSKey key = this.toWrite.get(0);

				this.toWrite.remove(0);
				this.written.add(key);

				write(key, this.document.getObject(key));
			}
		} catch (IOException e) {
			throw new VeraPDFParserException(StringExceptions.WRITE_ERROR);
		}
	}

	public void freeObjects(final Map<COSKey, Long> keys) {
		for (Map.Entry<COSKey, Long> entry : keys.entrySet()) {
			addXRef(entry.getKey(), entry.getValue(), 'f');
		}
	}

	public void setTrailer(final COSTrailer trailer) {
		setTrailer(trailer, 0);
	}

	public void setTrailer(final COSTrailer trailer, final long prev) {
		COSObject element = new COSObject();
		COSCopier copier = new COSCopier(element);
		trailer.getObject().accept(copier);

		this.info.getTrailer().setObject(element);

		this.info.getTrailer().setPrev(prev);

		if (prev == 0) {
			this.info.getTrailer().removeKey(ASAtom.ID);
		}
	}

	public void writeXRefInfo() {
		try {
			this.info.setStartXRef(getOffset() + incrementalOffset);

			this.info.getTrailer().setSize(this.info.getXRefSection().next());

			this.write("xref"); this.write(EOL); this.write(info.getXRefSection());
			this.write("trailer"); this.write(EOL); this.write(this.info.getTrailer().getObject()); this.write(EOL);
			this.write("startxref"); this.write(EOL); this.write(this.info.getStartXRef()); this.write(EOL);
			this.write("%%EOF"); this.write(EOL);
		} catch (IOException e) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected long getOffset() {
		try {
			return this.os.getOffset();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	protected void write(final COSKey key, final COSObject object) throws IOException {
		addXRef(key);
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
		long idTime = System.currentTimeMillis();
		try	{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(Long.toString(idTime).getBytes(StandardCharsets.ISO_8859_1));
			COSObject idString = COSString.construct(md5.digest(), true);
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
		this.info.getXRefSection().add(getKeyToWrite(key), offset, free);
	}

	public void addXRef(final COSKey key) {
		addXRef(key, getOffset() + incrementalOffset, 'n');
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
		final COSKey newKey = getKeyToWrite(value);
		this.write(newKey.getNumber()); this.write(" "); this.write(newKey.getGeneration());
	}

	protected void write(final COSObject value) {
		value.accept(this);
	}

	protected void write(final COSXRefRange value) throws IOException {
		os.write(String.valueOf(value.start)).write(" ").write(String.valueOf(value.count)).write(EOL);
	}

	protected void write(final COSXRefEntry value) throws IOException {
		String offset = formatXrefOffset.format(value.offset);
		String generation = formatXrefGeneration.format(value.generation);
		os.write(offset.getBytes(StandardCharsets.ISO_8859_1));
		os.write(" ");
		os.write(generation.getBytes(StandardCharsets.ISO_8859_1));
		os.write(" ");
		os.write(String.valueOf(value.free).getBytes(StandardCharsets.US_ASCII));
		os.write(EOL);
	}

	protected void write(final COSXRefSection value) throws IOException {
		List<COSXRefRange> range = value.getRange();
		for (COSXRefRange entry : range) {
			write(entry);
			for (int j = entry.start; j < entry.next(); j++) {
				this.write(value.getEntry(j));
			}
		}
	}

}
