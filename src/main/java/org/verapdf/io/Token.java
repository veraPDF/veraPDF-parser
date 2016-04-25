package org.verapdf.io;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class Token {

	public Type type;
	public String token;
	public long integer;
	public double real;
	public Keyword keyword;

	//fields specific for pdf/a validation of strings
	private boolean containsOnlyHex = true;
	private long hexCount = 0;

	public void toKeyword() {
		this.type = Type.TT_KEYWORD;
		this.keyword = getKeyword(token);
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

	private static final Map<Keyword, String> keywords = new HashMap<Keyword, String>();

	static {
		keywords.put(Keyword.KW_NULL, "null");
		keywords.put(Keyword.KW_TRUE, "true");
		keywords.put(Keyword.KW_FALSE, "false");
		keywords.put(Keyword.KW_STREAM, "stream");
		keywords.put(Keyword.KW_ENDSTREAM, "endstream");
		keywords.put(Keyword.KW_OBJ, "obj");
		keywords.put(Keyword.KW_ENDOBJ, "endobj");
		keywords.put(Keyword.KW_R, "R");
		keywords.put(Keyword.KW_N, "n");
		keywords.put(Keyword.KW_F, "f");
		keywords.put(Keyword.KW_XREF, "xref");
		keywords.put(Keyword.KW_STARTXREF, "startxref");
		keywords.put(Keyword.KW_TRAILER, "trailer");
		keywords.put(Keyword.KW_NONE, null);
	}

	public static Keyword getKeyword(final String keyword) {
		for (Map.Entry<Keyword, String> entry : keywords.entrySet()) {
			if (keyword.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		//TODO : not sure it's correct
		return Keyword.KW_NONE;
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
