package org.verapdf.io;

import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSObject;

import java.util.Queue;

/**
 * @author Timur Kamalov
 */
public class PDFParser extends Parser {

	private COSDocument cosDocument;
	private Queue<COSObject> objects;
	private Queue<Long> integers;

	public PDFParser(final COSDocument cosDocument, final String filename) { //tmp ??

	}

	public PDFParser(final String filename) { //tmp ??

	}


}
