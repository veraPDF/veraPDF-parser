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
package org.verapdf.io;

import org.verapdf.as.ASAtom;
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.cos.*;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.exceptions.InvalidPasswordException;
import org.verapdf.parser.DecodedObjectStreamParser;
import org.verapdf.parser.PDFParser;
import org.verapdf.parser.XRefReader;
import org.verapdf.pd.encryption.PDEncryption;
import org.verapdf.pd.encryption.StandardSecurityHandler;
import org.verapdf.tools.resource.FileResourceHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class Reader extends XRefReader {

	private static final Logger LOGGER = Logger.getLogger(Reader.class.getCanonicalName());

	private PDFParser parser;
	private COSHeader header;
	private Map<Long, DecodedObjectStreamParser> objectStreams;

	public Reader(final COSDocument document, final String fileName) throws IOException {
		super();
		this.parser = new PDFParser(document, fileName);
		this.objectStreams = new HashMap<>();
		init();
	}

	public Reader(final COSDocument document, final InputStream fileStream) throws IOException {
		super();
		this.parser = new PDFParser(document, fileStream);
		this.objectStreams = new HashMap<>();
		init();
	}

	//PUBLIC METHODS
	@Override
	public COSHeader getHeader() {
		return this.header;
	}

	@Override
	public COSObject getObject(final COSKey key) throws IOException {
		if (!super.containsKey(key)) {
			LOGGER.log(Level.FINE, "Trying to get object " + key.getNumber() + " " +
					key.getGeneration() + " that is not present in the document");
			return null;
		}
		long offset = getOffset(key).longValue();
		if (offset == 0) {
			return new COSObject();
		} else if(offset > 0) {
			if (header.getHeaderOffset() > 0) {
				offset += header.getHeaderOffset();
			}
			COSObject result = getObject(offset);
			result.setObjectKey(key);
			return result;
		}
		//TODO : set object key
		DecodedObjectStreamParser parser = objectStreams.get(Long.valueOf(-offset));
		if(parser != null) {
			return parser.getObject(key);
		}
		COSKey newKey = new COSKey(- (int)offset, 0);
		COSObject object = getObject(newKey);
		if(object == null || !object.getType().equals(COSObjType.COS_STREAM)) {
			throw new IOException("Object number " + (-offset) + " should" +
					" be object stream, but in fact it is " +
					(object == null ? "null" : object.getType()));
		}
		COSStream objectStream = (COSStream) object.getDirectBase();
		parser = new DecodedObjectStreamParser(
				objectStream.getData(COSStream.FilterFlags.DECODE),
				objectStream, new COSKey((int) -offset, 0),
				this.parser.getDocument());
		objectStreams.put(Long.valueOf(-offset), parser);
		return parser.getObject(key);
	}

	@Override
	public COSObject getObject(final long offset) throws IOException {
		return this.parser.getObject(offset);
	}

	@Override
	public boolean isLinearized() {
		return this.parser.isLinearized();
	}

	@Override
	public SeekableInputStream getPDFSource() {
		return this.parser.getPDFSource();
	}

	@Override
	public long getLastTrailerOffset() {
		long res = this.parser.getLastTrailerOffset();
		if (res == 0) {
			LOGGER.log(Level.FINE, "Offset of last trailer can not be determined");
		}
		return res;
	}

	// PRIVATE METHODS
	private void init() throws IOException {
		this.header = this.parser.getHeader();

		List<COSXRefInfo> infos = new ArrayList<>();
		this.parser.getXRefInfo(infos);
		setXRefInfo(infos);

		if(this.parser.isEncrypted()) {
			if(!docCanBeDecrypted()) {
				this.getPDFSource().close();
				if (this.parser.getDocument() != null) {
					FileResourceHandler handler =
							this.parser.getDocument().getResourceHandler();
					if (handler != null) {
						handler.close();
					}
				}
				throw new InvalidPasswordException("Reader::init(...)" + StringExceptions.ENCRYPTED_PDF_NOT_SUPPORTED);
			}
		}
	}

	private boolean docCanBeDecrypted() {
		try {
			COSObject cosEncrypt = this.parser.getEncryption();
			if (cosEncrypt.isIndirect().booleanValue()) {
				cosEncrypt = this.parser.getObject(this.getOffset(cosEncrypt.getObjectKey()).longValue());
			}
			PDEncryption encryption = new PDEncryption(cosEncrypt);
			if (encryption.getFilter() != ASAtom.STANDARD) {
				return false;
			}
			StandardSecurityHandler ssh = new StandardSecurityHandler(encryption,
					this.parser.getId());
			boolean res = ssh.isEmptyStringPassword();
			if (res) {
				this.parser.getDocument().setStandardSecurityHandler(ssh);
			}
			return res;
		} catch (IOException e) {
			LOGGER.log(Level.FINE, "Cannot read object " + this.parser.getEncryption().getKey(), e);
			return false;
		}
	}

	@Override
	public int getGreatestKeyNumberFromXref() {
		int res = 1;
		for (COSKey key : this.getKeys()) {
			if (key.getNumber() > res) {
				res = key.getNumber();
			}
		}
		return res;
	}

	@Override
	public void close() throws IOException {
		if (objectStreams != null) {
			for (Map.Entry<Long, DecodedObjectStreamParser> entry : this.objectStreams.entrySet()) {
				entry.getValue().closeInputStream();
			}
		}
	}
}
