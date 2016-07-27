package org.verapdf.cos;

import org.verapdf.cos.visitor.Writer;
import org.verapdf.cos.xref.COSXRefTable;
import org.verapdf.io.IReader;
import org.verapdf.io.Reader;
import org.verapdf.pd.PDDocument;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class COSDocument {

	private PDDocument doc;
	private IReader reader;
	private COSHeader header;
	private COSBody body;
	private COSXRefTable xref;
	private COSTrailer trailer;
	private boolean isNew;

	private byte postEOFDataSize;

	private boolean xrefEOLMarkersComplyPDFA;
	private boolean subsectionHeaderSpaceSeparated;

	public COSDocument(final PDDocument document) {
		this.doc = document;
		this.header = new COSHeader();
		this.body = new COSBody();
		this.xref = new COSXRefTable();
		this.trailer = new COSTrailer();
		this.isNew = true;

		this.xrefEOLMarkersComplyPDFA = true;
		this.subsectionHeaderSpaceSeparated = true;
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

		this.header = new COSHeader(this.reader.getHeader());
		this.xref = new COSXRefTable();
		this.xref.set(this.reader.getKeys());
		this.trailer = reader.getTrailer();

		this.xrefEOLMarkersComplyPDFA = true;
		this.subsectionHeaderSpaceSeparated = true;
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
		COSKey key = new COSKey();
		try {
			List<COSObject> objects = this.body.getAll();
			for (COSObject object : objects) {
				if (!object.empty()) {
					result.add(object);
					continue;
				}

				key = object.getKey();
				COSObject newObj = this.reader.getObject(key);
				this.body.set(key, newObj);
				result.add(newObj);
			}
		} catch (IOException e) {
			//TODO :
			throw new RuntimeException("Error while parsing object : " + key.getNumber() +
					" " + key.getGeneration());
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

			this.body.set(key, newObj);
			return this.body.get(key);
		} catch (IOException e) {
			//TODO :
			throw new RuntimeException("Error while parsing object : " + key.getNumber() +
									   " " + key.getGeneration());
		}
	}

	public void setObject(final COSKey key, final COSObject obj) {
		this.body.set(key, obj);
		this.xref.newKey(key);
	}

	public COSKey setObject(COSObject obj) {
		COSKey key = obj.getKey();

		if (key.equals(new COSKey())) {
			key = this.xref.next();
			this.body.set(key, obj.getDirect());
			obj = COSIndirect.construct(key, this);
		}

		this.xref.newKey(key);
		return key;
	}

	public COSTrailer getTrailer() {
		return this.trailer;
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

	public void saveAs(final Writer writer) {
		writer.writeHeader(this.header.getHeader());

		writer.addToWrite(this.xref.getAllKeys());
		writer.writeBody();

		writer.setTrailer(this.trailer);

		writer.writeXRefInfo();

		writer.clear();
	}

}
