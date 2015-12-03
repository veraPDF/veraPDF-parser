package org.verapdf.io;

import java.io.FileNotFoundException;

/**
 * @author Timur Kamalov
 */
public class Parser {

	private InternalInputStream stream;

	public Parser(String fileName) throws FileNotFoundException {
		this.stream = new InternalInputStream(fileName);
	}




}
