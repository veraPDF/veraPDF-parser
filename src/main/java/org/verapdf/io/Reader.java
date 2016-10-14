package org.verapdf.io;

import org.apache.log4j.Logger;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class Reader extends XRefReader {

	private static final Logger LOGGER = Logger.getLogger(Reader.class);

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
	public COSHeader getHeader() {
		return this.header;
	}

	public COSObject getObject(final COSKey key) throws IOException {
		if (!super.containsKey(key)) {
			LOGGER.debug("Trying to get object " + key.getNumber() + " " +
					key.getGeneration() + " that is not present in the document");
			return null;
		}
		long offset = getOffset(key);
		if(offset > 0) {
			if (getHeader().getHeaderOffset() > 0) {
				offset += getHeader().getHeaderOffset();
			}
			COSObject result = getObject(offset);
			result.setObjectKey(key);
			return result;
		} else {
			//TODO : set object key
			DecodedObjectStreamParser parser = objectStreams.get(-offset);
			if(parser != null) {
				return parser.getObject(key.getNumber());
			} else {
				COSKey newKey = new COSKey(- (int)offset, 0);
				COSObject object = getObject(newKey);
				if(object == null || !object.getType().equals(COSObjType.COS_STREAM)) {
					throw new IOException("Object number " + (-offset) + " should" +
							" be object stream, but in fact it is " +
							(object == null ? "null" : object.getType()));
				}
				COSStream objectStream = (COSStream) object.get();
				parser = new DecodedObjectStreamParser(
						objectStream.getData(COSStream.FilterFlags.DECODE),
						objectStream, new COSKey((int) -offset, 0),
						this.parser.getDocument());
				objectStreams.put(-offset, parser);
				return parser.getObject(key.getNumber());
			}
		}
	}

	public COSObject getObject(final long offset) throws IOException {
		return this.parser.getObject(offset);
	}

	public boolean isLinearized() {
		return this.parser.isLinearized();
	}

	@Override
	public SeekableStream getPDFSource() {
		return this.parser.getPDFSource();
	}

	// PRIVATE METHODS
	private void init() throws IOException {
		this.header = this.parser.getHeader();

		List<COSXRefInfo> infos = new ArrayList<COSXRefInfo>();
		this.parser.getXRefInfo(infos);
		setXRefInfo(infos);

		if(this.parser.isEncrypted()) {
			if(!docCanBeDecrypted()) {
				throw new InvalidPasswordException("Reader::init(...)" + StringExceptions.ENCRYPTED_PDF_NOT_SUPPORTED);
			}
		}
	}

	private boolean docCanBeDecrypted() {
		try {
			COSObject cosEncrypt = this.parser.getEncryption();
			if (cosEncrypt.isIndirect()) {
				cosEncrypt = this.parser.getObject(this.getOffset(cosEncrypt.getObjectKey()));
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
			LOGGER.debug("Cannot read object " + this.parser.getEncryption().getKey());
			return false;
		}
	}

}
