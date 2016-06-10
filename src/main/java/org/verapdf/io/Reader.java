package org.verapdf.io;

import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSHeader;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.parser.PDFParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class Reader extends XRefReader {

	private PDFParser parser;
	private COSHeader header;

	public Reader(final COSDocument document, final String fileName) throws Exception {
		super();
		this.parser = new PDFParser(document, fileName);
		init();
	}

	public Reader(final COSDocument document, final InputStream fileStream) throws Exception {
		super();
		this.parser = new PDFParser(document, fileStream);
		init();
	}

	//PUBLIC METHODS
	public String getHeader() {
		return this.header.getHeader();
	}

	public COSObject getObject(final COSKey key) throws IOException {
		final long offset = getOffset(key);
		return getObject(offset);
	}

	public COSObject getObject(final long offset) throws IOException {
		return this.parser.getObject(offset);
	}


	// PRIVATE METHODS
	private void init() throws Exception {
		this.header = this.parser.getHeader();

		List<COSXRefInfo> infos = new ArrayList<COSXRefInfo>();
		this.parser.getXRefInfo(infos);
		setXRefInfo(infos);
	}

}
