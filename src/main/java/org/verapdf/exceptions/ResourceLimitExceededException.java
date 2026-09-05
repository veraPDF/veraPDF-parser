/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.exceptions;

/**
 * Thrown when parsing is aborted because a configured resource limit is exceeded, as opposed to the
 * input being malformed. It is a subclass of {@link VeraPDFParserException}, so existing callers that
 * catch the parser exception keep working unchanged; a caller that needs to tell a deliberate
 * resource-limit abort (for example the caps set through {@code SeekableInputStream.setMaxStreamSize}
 * or {@code COSDocument.setMaxNumberOfObjects}) apart from a genuine parse error can catch this type
 * instead of matching on the message text.
 *
 * @author veraPDF Consortium
 */
public class ResourceLimitExceededException extends VeraPDFParserException {

	public ResourceLimitExceededException() {
	}

	public ResourceLimitExceededException(String message) {
		super(message);
	}

	public ResourceLimitExceededException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceLimitExceededException(Throwable cause) {
		super(cause);
	}
}
