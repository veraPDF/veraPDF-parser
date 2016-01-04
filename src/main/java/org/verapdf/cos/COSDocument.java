package org.verapdf.cos;

import org.verapdf.io.IReader;
import org.verapdf.io.Reader;
import org.verapdf.pd.PDDocument;

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

	public COSDocument(PDDocument doc) {
		this.doc = doc;
		this.reader = new Reader()
	}

	public COSDocument(String fileName, PDDocument doc) {
	}

	public String getHeader() {
		return this.header.get();
	}

	public void setHeader(String header) {
		this.header = new COSHeader(header);
	}

	public COSObject getObject(final COSKey key) {
		COSObject obj = this.body.get(key);
		if (!obj.empty()) {
			return obj;
		}

		COSObject newObj = this.reader.getObject(key);

		this.body.set(key, newObj);
		return this.body.get(key);
	}

	public void setObject(final COSKey key, final COSObject obj) {
		this.body.set(key, obj);
		this.xref.newKey(key);
	}

	public COSKey setObject(COSObject obj) {
		COSKey key = obj.getKey();

		//TODO : equals here
		if (key == new COSKey()) {
			key = this.xref.next();
			this.body.set(key, obj.getDirect());
			obj = COSIndirect.construct(key, this);
		}

		this.xref.newKey(key);
		return key;
	}

}
