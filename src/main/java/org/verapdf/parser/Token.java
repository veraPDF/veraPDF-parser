package org.verapdf.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class Token {

	public Type type;
	public Keyword keyword;

	public long integer;
	public double real;

	private StringBuilder token = new StringBuilder();

	//fields specific for pdf/a validation of strings
	private boolean containsOnlyHex = true;
	private long hexCount = 0;

	public void toKeyword() {
		this.type = Type.TT_KEYWORD;
		this.keyword = getKeyword(token.toString());
	}

	public void append(char c) {
		this.token.append(c);
	}

	public String getValue() {
		return this.token.toString();
	}

	public void clearValue() {
		this.token.setLength(0);
	}

	public enum Type {
		TT_NONE,
		TT_KEYWORD,
		TT_INTEGER,
		TT_REAL,
		TT_LITSTRING,
		TT_HEXSTRING,
		TT_NAME,
		TT_OPENARRAY,
		TT_CLOSEARRAY,
		TT_OPENDICT,
		TT_CLOSEDICT,
		TT_EOF
	}

	public enum Keyword {
		KW_NONE,
		KW_NULL,
		KW_TRUE,
		KW_FALSE,
		KW_STREAM,
		KW_ENDSTREAM,
		KW_OBJ,
		KW_ENDOBJ,
		KW_R,
		KW_N,
		KW_F,
		KW_XREF,
		KW_STARTXREF,
		KW_TRAILER
	}

	private static final Map<String, Keyword> keywords = new HashMap<>();

	static {
		keywords.put("null", Keyword.KW_NULL);
		keywords.put("true", Keyword.KW_TRUE);
		keywords.put("false", Keyword.KW_FALSE);
		keywords.put("stream", Keyword.KW_STREAM);
		keywords.put("endstream", Keyword.KW_ENDSTREAM);
		keywords.put("obj", Keyword.KW_OBJ );
		keywords.put("endobj", Keyword.KW_ENDOBJ);
		keywords.put("R", Keyword.KW_R );
		keywords.put("n", Keyword.KW_N);
		keywords.put("f", Keyword.KW_F);
		keywords.put("xref", Keyword.KW_XREF);
		keywords.put("startxref", Keyword.KW_STARTXREF);
		keywords.put("trailer", Keyword.KW_TRAILER);
		keywords.put(null, Keyword.KW_NONE);
	}

	public static Keyword getKeyword(final String keyword) {
		return keywords.get(keyword);
	}

	//GETTERS & SETTERS
	public boolean isContainsOnlyHex() {
		return containsOnlyHex;
	}

	public void setContainsOnlyHex(boolean containsOnlyHex) {
		this.containsOnlyHex = containsOnlyHex;
	}

	public Long getHexCount() {
		return hexCount;
	}

	public void setHexCount(Long hexCount) {
		this.hexCount = hexCount;
	}

}
