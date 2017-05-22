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
package org.verapdf.as;

/**
 * Class contains tools for char processing.
 *
 * @author Timur Kamalov
 */
public class CharTable {

	private static final byte SPACE = 1;
	private static final byte DELIMITER	= 2;
	private static final byte NUMERIC = 4;
	private static final byte OCTALDIGIT = 8;

	private static final byte[] charAttributes = {
		/*	0		NUL	*/	(0 | SPACE) ,
		/*	1		SOH	*/	0 ,
		/*	2		SIX	*/	0 ,
		/*	3		EIX	*/	0 ,
		/*	4		EOI	*/	0 ,
		/*	5		ENQ	*/	0 ,
		/*	6		ACK	*/	0 ,
		/*	7		BEL	*/	0 ,
		/*	8		BS		*/	0 ,
		/*	9		HT		*/	(0 | SPACE) ,
		/*	10		LF		*/	(0 | SPACE) ,
		/*	11		VT		*/	0 ,
		/*	12		FF		*/	(0 | SPACE) ,
		/*	13		CR		*/	(0 | SPACE) ,
		/*	14		SO		*/	0 ,
		/*	15		SI		*/	0 ,
		/*	16		SLE	*/	0 ,
		/*	17		CS1	*/	0 ,
		/*	18		DC2	*/	0 ,
		/*	19		DC3	*/	0 ,
		/*	20		DC4	*/	0 ,
		/*	21		NAK	*/	0 ,
		/*	22		SYN	*/	0 ,
		/*	23		ETB	*/	0 ,
		/*	24		CAN	*/	0 ,
		/*	25		EM		*/	0 ,
		/*	26		STB	*/	0 ,
		/*	27		ESC	*/	0 ,
		/*	28		FS		*/	0 ,
		/*	29		GS		*/	0 ,
		/*	30		RS		*/	0 ,
		/*	31		US		*/	0 ,
		/*	32		' '	*/	(0 | SPACE) ,
		/*	33		'!'	*/	0 ,
		/*	34		'"'	*/	0 ,
		/*	35		'#'	*/	(0 | NUMERIC) ,
		/*	36		'$'	*/	0 ,
		/*	37		'%'	*/	(0 | DELIMITER) ,
		/*	38		'|'	*/	0 ,
		/*	39		'''	*/	0 ,
		/*	40		'('	*/	(0 | DELIMITER) ,
		/*	41		')'	*/	(0 | DELIMITER) ,
		/*	42		'*'	*/	0 ,
		/*	43		'+'	*/	(0 | NUMERIC) ,
		/*	44		','	*/	0 ,
		/*	45		'-'	*/	(0 | NUMERIC) ,
		/*	46		'.'	*/	(0 | NUMERIC) ,
		/*	47		'/'	*/	(0 | DELIMITER) ,
		/*	48		'0'	*/	(0 | NUMERIC | OCTALDIGIT) ,
		/*	49		'1'	*/	(0 | NUMERIC | OCTALDIGIT) ,
		/*	50		'2'	*/	(0 | NUMERIC | OCTALDIGIT) ,
		/*	51		'3'	*/	(0 | NUMERIC | OCTALDIGIT) ,
		/*	52		'4'	*/	(0 | NUMERIC | OCTALDIGIT) ,
		/*	53		'5'	*/	(0 | NUMERIC | OCTALDIGIT) ,
		/*	54		'6'	*/	(0 | NUMERIC | OCTALDIGIT) ,
		/*	55		'7'	*/	(0 | NUMERIC | OCTALDIGIT) ,
		/*	56		'8'	*/	(0 | NUMERIC) ,
		/*	57		'9'	*/	(0 | NUMERIC) ,
		/*	58		':'	*/	0 ,
		/*	59		';'	*/	0 ,
		/*	60		'<'	*/	(0 | DELIMITER) ,
		/*	61		'='	*/	0 ,
		/*	62		'>'	*/	(0 | DELIMITER) ,
		/*	63		'?'	*/	0 ,
		/*	64		'@'	*/	0 ,
		/*	65		'A'	*/	0 ,
		/*	66		'B'	*/	0 ,
		/*	67		'C'	*/	0 ,
		/*	68		'D'	*/	0 ,
		/*	69		'E'	*/	(0 | NUMERIC) ,
		/*	70		'F'	*/	0 ,
		/*	71		'G'	*/	0 ,
		/*	72		'H'	*/	0 ,
		/*	73		'I'	*/	0 ,
		/*	74		'J'	*/	0 ,
		/*	75		'K'	*/	0 ,
		/*	76		'L'	*/	0 ,
		/*	77		'M'	*/	0 ,
		/*	78		'N'	*/	0 ,
		/*	79		'O'	*/	0 ,
		/*	80		'P'	*/	0 ,
		/*	81		'Q'	*/	0 ,
		/*	82		'R'	*/	0 ,
		/*	83		'S'	*/	0 ,
		/*	84		'T'	*/	0 ,
		/*	85		'U'	*/	0 ,
		/*	86		'V'	*/	0 ,
		/*	87		'W'	*/	0 ,
		/*	88		'X'	*/	0 ,
		/*	89		'Y'	*/	0 ,
		/*	90		'Z'	*/	0 ,
		/*	91		'['	*/	(0 | DELIMITER) ,
		/*	92		'\'	*/	0 ,
		/*	93		']'	*/	(0 | DELIMITER) ,
		/*	94		'^'	*/	0 ,
		/*	95		''		*/	0 ,
		/*	96		'`'	*/	0 ,
		/*	97		'a'	*/	0 ,
		/*	98		'b'	*/	0 ,
		/*	99		'c'	*/	0 ,
		/*	100	'd'	*/	0 ,
		/*	101	'e'	*/	(0 | NUMERIC) ,
		/*	102	'f'	*/	0 ,
		/*	103	'g'	*/	0 ,
		/*	104	'h'	*/	0 ,
		/*	105	'i'	*/	0 ,
		/*	106	'j'	*/	0 ,
		/*	107	'k'	*/	0 ,
		/*	108	'l'	*/	0 ,
		/*	109	'm'	*/	0 ,
		/*	110	'n'	*/	0 ,
		/*	111	'o'	*/	0 ,
		/*	112	'p'	*/	0 ,
		/*	113	'q'	*/	0 ,
		/*	114	'r'	*/	0 ,
		/*	115	's'	*/	0 ,
		/*	116	't'	*/	0 ,
		/*	117	'u'	*/	0 ,
		/*	118	'v'	*/	0 ,
		/*	119	'w'	*/	0 ,
		/*	120	'x'	*/	0 ,
		/*	121	'y'	*/	0 ,
		/*	122	'z'	*/	0 ,
		/*	123	'{'	*/	(0 | DELIMITER),
		/*	124	'|'	*/	0 ,
		/*	125	'}'	*/	(0 | DELIMITER),
		/*	126	'~'	*/	0 ,
		/*	127	DEL	*/	0 ,
		/*	128-255 */
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	};

	public static final byte ASCII_NUL = 0;
	public static final byte ASCII_BS = 8;
	public static final byte ASCII_HT = 9;
	public static final byte ASCII_LF = 10;
	public static final byte ASCII_FF = 12;
	public static final byte ASCII_CR = 13;
	public static final byte ASCII_SPACE = 32;

	public static final byte ASCII_ZERO = 48;
	public static final byte ASCII_NINE = 57;

	public static final byte ASCII_LEFT_PAR = 40;
	public static final byte ASCII_RIGHT_PAR = 41;

	private static byte getAttributes(int c) {
		return charAttributes[c];
	}

	/**
	 * Checks if character is a space character.
	 *
	 * @param c is character to check.
	 * @return true if c is a space character.
	 */
	public static boolean isSpace(int c) {
		if (c < 0 || c > 255) {
			return false;
		}
		return (getAttributes(c) & SPACE) != 0;
	}

	/**
	 * Checks if given character is a token delimiter.
	 *
	 * @param c is a character to check.
	 * @return true if c is a token delimiter.
	 */
	public static boolean isTokenDelimiter(int c) {
		if (c < 0 || c > 255) {
			return false;
		}
		return (getAttributes(c) & (SPACE | DELIMITER)) != 0;
	}

	/**
	 * Checks if given character is not a space and not a token delimiter.
	 *
	 * @param c is a character to check.
	 * @return true if c is a regular character.
	 */
	public static boolean isRegular(int c) {
		if (c < 0 || c > 255) {
			return false;
		}
		return (getAttributes(c) & (SPACE | DELIMITER)) == 0;
	}

}
