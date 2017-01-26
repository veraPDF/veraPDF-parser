/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public class COSHeader {

	private long headerOffset;

	private String header;

	private float version;

	private int headerCommentByte1;
	private int headerCommentByte2;
	private int headerCommentByte3;
	private int headerCommentByte4;

	public COSHeader() {
	}

	public COSHeader(final String header) {
		this.header = header;
	}

	public void setBinaryHeaderBytes(int first, int second, int third, int fourth) {
		this.headerCommentByte1 = first;
		this.headerCommentByte2 = second;
		this.headerCommentByte3 = third;
		this.headerCommentByte4 = fourth;
	}

	// GETTERS & SETTERS

	public long getHeaderOffset() {
		return headerOffset;
	}

	public void setHeaderOffset(long headerOffset) {
		this.headerOffset = headerOffset;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(final String header) {
		this.header = header;
	}

	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	public int getHeaderCommentByte1() {
		return headerCommentByte1;
	}

	public void setHeaderCommentByte1(final int headerCommentByte1) {
		this.headerCommentByte1 = headerCommentByte1;
	}

	public int getHeaderCommentByte2() {
		return headerCommentByte2;
	}

	public void setHeaderCommentByte2(final int headerCommentByte2) {
		this.headerCommentByte2 = headerCommentByte2;
	}

	public int getHeaderCommentByte3() {
		return headerCommentByte3;
	}

	public void setHeaderCommentByte3(final int headerCommentByte3) {
		this.headerCommentByte3 = headerCommentByte3;
	}

	public int getHeaderCommentByte4() {
		return headerCommentByte4;
	}

	public void setHeaderCommentByte4(final int headerCommentByte4) {
		this.headerCommentByte4 = headerCommentByte4;
	}

}
