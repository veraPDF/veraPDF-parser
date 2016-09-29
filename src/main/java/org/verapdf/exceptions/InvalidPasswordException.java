package org.verapdf.exceptions;

import java.io.IOException;

/**
 * @author Maksim Bezrukov
 */
public class InvalidPasswordException extends IOException {

	public InvalidPasswordException() {
	}

	public InvalidPasswordException(String message) {
		super(message);
	}

	public InvalidPasswordException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPasswordException(Throwable cause) {
		super(cause);
	}
}
