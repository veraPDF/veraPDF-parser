package org.verapdf.exceptions;

public class VeraPDFParserException extends RuntimeException {

	public VeraPDFParserException() {
	}

	public VeraPDFParserException(String message) {
		super(message);
	}

	public VeraPDFParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public VeraPDFParserException(Throwable cause) {
		super(cause);
	}
}
