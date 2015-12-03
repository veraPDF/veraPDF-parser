package org.verapdf.io;

/**
 * @author Timur Kamalov
 */
public class Token {

	public Type type;
	public String token;
	public long integer;
	public double real;
	public Keyword keyword;

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

}
