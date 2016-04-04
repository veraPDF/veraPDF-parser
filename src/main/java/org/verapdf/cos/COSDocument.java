package org.verapdf.cos;

import org.verapdf.cos.visitor.Writer;
import org.verapdf.cos.xref.COSXRefTable;
import org.verapdf.io.IReader;
import org.verapdf.io.Reader;
import org.verapdf.pd.PDDocument;

import java.io.IOException;

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

	public COSDocument(PDDocument doc) {
		this.doc = doc;
		this.header = new COSHeader();
		this.body = new COSBody();
		this.xref = new COSXRefTable();
		this.trailer = new COSTrailer();
		this.isNew = true;
	}

	public COSDocument(String fileName, PDDocument doc) throws Exception {
		this.doc = doc;
		this.body = new COSBody();

		this.reader = new Reader(this, fileName);

		this.header = new COSHeader(this.reader.getHeader());
		this.xref = new COSXRefTable();
		this.xref.set(this.reader.getKeys());
		this.trailer = reader.getTrailer();
	}

	public boolean isNew() {
		return this.isNew;
	}

	public String getHeader() {
		return this.header.get();
	}

	public void setHeader(String header) {
		this.header.set(header);
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

	public void save() {

	}

	public void saveAs(final Writer out) {
		out.writeHeader(getHeader());

		out.addToWrite(this.xref.getAllKeys());
		out.writeBody();

		out.setTrailer(this.trailer);

		out.writeXRefInfo();

		out.clear();
	}

}
