package org.verapdf.io;

import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSXRefInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class Reader extends XRefReader {

	private PDFParser parser;
	private String header;

	public Reader(final COSDocument document, final String fileName) throws Exception {
		super();
		this.parser = new PDFParser(document, fileName);
		init();
	}

	//PUBLIC METHODS
	public String getHeader() {
		return this.header;
	}

	public COSObject getObject(final COSKey key) throws IOException {
		final long offset = getOffset(key);
		return getObject(offset);
	}

	public COSObject getObject(final long offset) throws IOException {
		return this.parser.getObject((int) offset);
	}


	// PRIVATE METHODS
	private void init() throws Exception {
		this.header = this.parser.getHeader();

		List<COSXRefInfo> infos = new ArrayList<COSXRefInfo>();
		this.parser.getXRefInfo(infos);
		setXRefInfo(infos);
	}

}
