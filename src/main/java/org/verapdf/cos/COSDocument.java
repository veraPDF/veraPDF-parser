package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.cos.visitor.Writer;
import org.verapdf.cos.xref.COSXRefTable;
import org.verapdf.io.IReader;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.Reader;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.PDDocument;
import org.verapdf.pd.encryption.StandardSecurityHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class COSDocument {

	private static final Logger LOGGER = Logger.getLogger(COSDocument.class.getCanonicalName());

	private PDDocument doc;
	private IReader reader;
	private COSHeader header;
	private COSBody body;
	private COSXRefTable xref;
	private COSTrailer trailer;
	private COSTrailer firstTrailer;
	private COSTrailer lastTrailer;
	private boolean linearized;
	private boolean isNew;
	private StandardSecurityHandler standardSecurityHandler;
	private List<COSObject> changedObjects;
	private List<COSObject> addedObjects;

	private byte postEOFDataSize;

	private boolean xrefEOLMarkersComplyPDFA = true;
	private boolean subsectionHeaderSpaceSeparated = true;

	public COSDocument(final PDDocument document) {
		this.doc = document;
		this.header = new COSHeader();
		this.body = new COSBody();
		this.xref = new COSXRefTable();
		this.trailer = new COSTrailer();
		this.firstTrailer = new COSTrailer();
		this.lastTrailer = new COSTrailer();
		this.linearized = false;
		this.isNew = true;
		this.changedObjects = new ArrayList<>();
		this.addedObjects = new ArrayList<>();
	}

	public COSDocument(final String fileName, final PDDocument document) throws IOException {
		initReader(fileName);

		initCOSDocument(document);
	}

	public COSDocument(final InputStream fileStream, final PDDocument document) throws IOException {
		initReader(fileStream);

		initCOSDocument(document);
	}

	private void initCOSDocument(final PDDocument document) {
		this.doc = document;
		this.body = new COSBody();

		this.header = this.reader.getHeader();
		this.xref = new COSXRefTable();
		this.xref.set(this.reader.getKeys());
		this.trailer = reader.getTrailer();
		this.firstTrailer = reader.getFirstTrailer();
		this.lastTrailer = reader.getLastTrailer();
		this.linearized = reader.isLinearized();
		this.changedObjects = new ArrayList<>();
		this.addedObjects = new ArrayList<>();
	}

	private void initReader(final InputStream fileStream) throws IOException {
		this.reader = new Reader(this, fileStream);
	}

	private void initReader(final String fileName) throws IOException {
		this.reader = new Reader(this, fileName);
	}

	public boolean isNew() {
		return this.isNew;
	}

	public void setHeader(String header) {
		this.header.setHeader(header);
	}

	public List<COSObject> getObjects() {
		List<COSObject> result = new ArrayList<>();
		for (COSKey key : this.xref.getAllKeys()) {
			COSObject obj = this.body.get(key);
			if (!obj.empty()) {
				result.add(obj);
			} else {
				try {
					COSObject newObj = this.reader.getObject(key);

					this.body.set(key, newObj);
					result.add(newObj);
				} catch (IOException e) {
					LOGGER.log(Level.FINE, "Error while parsing object : " + key.getNumber() +
							" " + key.getGeneration(), e);
				}
			}
		}
		return result;
	}

	public List<COSObject> getObjectsByType(ASAtom type) {
		List<COSObject> result = new ArrayList<>();
		for (COSKey key : this.xref.getAllKeys()) {
			COSObject obj = this.body.get(key);
			if (!obj.empty()) {
				addObjectWithTypeKeyCheck(result, obj, type);
			} else {
				try {
					COSObject newObj = this.reader.getObject(key);

					this.body.set(key, newObj);
					addObjectWithTypeKeyCheck(result, obj, type);
				} catch (IOException e) {
					LOGGER.log(Level.FINE, "Error while parsing object : " + key.getNumber() +
							" " + key.getGeneration(), e);
				}
			}
		}
		return result;
	}

	private static void addObjectWithTypeKeyCheck(List<COSObject> objects,
												  COSObject obj, ASAtom type) {
		if (obj != null && !obj.empty() && obj.getType().isDictionaryBased()) {
			ASAtom actualType = obj.getNameKey(ASAtom.TYPE);
			if (actualType == type) {
				objects.add(obj);
			}
		}
	}

	public Map<COSKey, COSObject> getObjectsMap() {
		Map<COSKey, COSObject> result = new HashMap<>();
		for (COSKey key : this.xref.getAllKeys()) {
			COSObject obj = this.body.get(key);
			if (!obj.empty()) {
				result.put(key, obj);
			} else {
				try {
					COSObject newObj = this.reader.getObject(key);

					this.body.set(key, newObj);
					result.put(key, newObj);
				} catch (IOException e) {
					LOGGER.log(Level.FINE, "Error while parsing object : " + key.getNumber() +
							" " + key.getGeneration(), e);
				}
			}
		}
		return result;
	}

	public COSObject getObject(final COSKey key) {
		try {
			COSObject obj = this.body.get(key);
			if (!obj.empty()) {
				return obj;
			}

			COSObject newObj = this.reader.getObject(key);
			if (newObj == null) {
				return COSObject.getEmpty();
			}
			this.body.set(key, newObj);
			return this.body.get(key);
		} catch (IOException e) {
			//TODO : maybe not runtime, maybe no exception at all
			throw new RuntimeException("Error while parsing object : " + key.getNumber() +
									   " " + key.getGeneration(), e);
		}
	}

	public Long getOffset(final COSKey key) {
		return this.reader.getOffset(key);
	}

	public void setObject(final COSKey key, final COSObject obj) {
		this.body.set(key, obj);
		this.xref.newKey(key);
	}

	public COSKey setObject(COSObject obj) {
		COSKey key = obj.getKey();

		//TODO : fix this method for document save
		if (key == null) {
			key = this.xref.next();
			this.body.set(key, obj.isIndirect() ? obj.getDirect() : obj);
			obj = COSIndirect.construct(key, this);
		}

		this.xref.newKey(key);
		return key;
	}

	public COSTrailer getTrailer() {
		return this.trailer;
	}

	public COSTrailer getFirstTrailer() {
		return firstTrailer;
	}

	public COSTrailer getLastTrailer() {
		return lastTrailer;
	}

	public boolean isLinearized() {
		return linearized;
	}

	public PDDocument getPDDocument() {
		return this.doc;
	}

	public COSHeader getHeader() {
		return header;
	}

	public void setHeader(COSHeader header) {
		this.header = header;
	}

	public byte getPostEOFDataSize() {
		return postEOFDataSize;
	}

	public void setPostEOFDataSize(byte postEOFDataSize) {
		this.postEOFDataSize = postEOFDataSize;
	}

	public boolean isXrefEOLMarkersComplyPDFA() {
		return xrefEOLMarkersComplyPDFA;
	}

	public void setXrefEOLMarkersComplyPDFA(boolean xrefEOLMarkersComplyPDFA) {
		this.xrefEOLMarkersComplyPDFA = xrefEOLMarkersComplyPDFA;
	}

	public boolean isSubsectionHeaderSpaceSeparated() {
		return subsectionHeaderSpaceSeparated;
	}

	public void setSubsectionHeaderSpaceSeparated(boolean subsectionHeaderSpaceSeparated) {
		this.subsectionHeaderSpaceSeparated = subsectionHeaderSpaceSeparated;
	}

	public void save() {
		//TODO : implement this
	}

	public SeekableInputStream getPDFSource() {
		return this.reader.getPDFSource();
	}

	public void saveAs(final Writer writer) {
		writer.writeHeader(this.header.getHeader());

		writer.addToWrite(this.xref.getAllKeys());
		writer.writeBody();

		writer.setTrailer(this.trailer);

		writer.writeXRefInfo();

		writer.clear();
	}

	public void saveTo(final OutputStream stream) {
		try {
			File temp = File.createTempFile("tmp_pdf_file", ".pdf");
			temp.deleteOnExit();
			Writer pdfWriter = new Writer(this, temp.getAbsolutePath(),
					this.getPDFSource().getStreamLength());
			pdfWriter.writeIncrementalUpdate(changedObjects, addedObjects);
			pdfWriter.close();
			this.getPDFSource().reset();
			writeInputIntoOutput(this.getPDFSource(), stream);
			InternalInputStream pdf = new InternalInputStream(temp.getAbsolutePath());
			writeInputIntoOutput(pdf, stream);
			pdf.close();
		} catch (IOException e) {
			LOGGER.log(Level.FINE, "Can't write COSDocument to stream", e);
		}
	}

	private static void writeInputIntoOutput(InputStream input, OutputStream output) throws IOException {
		byte[] buf = new byte[ASBufferingInFilter.BF_BUFFER_SIZE];
		int read = input.read(buf, 0, buf.length);
		while (read != -1) {
			output.write(buf, 0, read);
			read = input.read(buf, 0, buf.length);
		}
	}

	public void setStandardSecurityHandler(StandardSecurityHandler standardSecurityHandler) {
		this.standardSecurityHandler = standardSecurityHandler;
	}

	public StandardSecurityHandler getStandardSecurityHandler() {
		return standardSecurityHandler;
	}

	public boolean isEncrypted() {
		return this.standardSecurityHandler != null;
	}

	public COSArray getID() {
		if (trailer != null) {
			COSObject res = trailer.getKey(ASAtom.ID);
			if (res.getType() == COSObjType.COS_ARRAY) {
				return (COSArray) res.getDirectBase();
			}
		}
		return null;
	}

	public void addObject(COSObject obj) {
		if (obj != null && !obj.empty()) {
			this.addedObjects.add(obj);
		}
	}

	public void removeAddedObject(COSObject obj) {
		this.addedObjects.remove(obj);
	}

	public void addChangedObject(COSObject obj) {
		if (obj != null && !obj.empty() && !isObjectChanged(obj)) {
			this.changedObjects.add(obj);
		}
	}

	public void removeChangedObject(COSObject obj) {
		this.changedObjects.remove(obj);
	}

	public boolean isObjectChanged(COSObject obj) {
		return listContainsObject(changedObjects, obj) ||
				listContainsObject(addedObjects, obj);
	}

	private static boolean listContainsObject(List<COSObject> list, COSObject obj) {
		for (COSObject listObject : list) {
			if (listObject == obj) {
				return true;
			}
		}
		return false;
	}

	public long getLastTrailerOffset() {
		return this.reader.getLastTrailerOffset();
	}

	public int getLastKeyNumber() {
		return this.reader.getGreatestKeyNumberFromXref();
	}
}
