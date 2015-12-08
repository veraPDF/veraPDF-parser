package org.verapdf.as;

/**
 * @author Timur Kamalov
 */
public class CharTable {

	//TODO : write some structure with char attributes

	//SPACES
	public static final byte ASCII_NUL = 0;
	public static final byte ASCII_BS = 8;
	public static final byte ASCII_HT = 9;
	public static final byte ASCII_LF = 10;
	public static final byte ASCII_FF = 12;
	public static final byte ASCII_CR = 13;
	public static final byte ASCII_SPACE = 32;

	public static boolean isSpace(char c) {
		if (c == ASCII_NUL || c == ASCII_HT || c == ASCII_LF ||
				c == ASCII_FF || c == ASCII_CR || c == ASCII_SPACE) {
			return true;
		}
		return false;
	}

	public static boolean isTokenDelimiter(char c) {
		if (isSpace(c) || c == 37 || c == 40 || c == 41 ||
				c == 47 || c == 60 || c == 62 || c == 91 ||
				c == 93 || c == 123 || c == 125) {
			return true;
		}
		return false;
	}

}
