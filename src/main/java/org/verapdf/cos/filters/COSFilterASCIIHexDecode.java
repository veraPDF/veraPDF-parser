package org.verapdf.cos.filters;

/**
 * @author Timur Kamalov
 */
public class COSFilterASCIIHexDecode {

	public final static byte ws = 17;
	public final static byte er = 127;

	private final static byte[] loHexTable = {
			ws, er, er, er, er, er, er, er, er, ws, ws, er, ws, ws, er, er, 	// 0  - 15
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 16 - 31
			ws, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 32 - 47
			0,  1,  2,  3,  4,  5,  6,  7,  8,  9,  er, er, er, er, er, er, 	// 48 - 63
			er, 10, 11, 12, 13, 14, 15, er, er, er, er, er, er, er, er, er, 	// 64 - 79
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 80 - 95
			er, 10, 11, 12, 13, 14, 15, er, er, er, er, er, er, er, er, er, 	// 96 - 111
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 112 - 127
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 128 - 143
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 144 - 159
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 160 - 175
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 176 - 191
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 192 - 207
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 208 - 223
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, 	// 224 - 239
			er, er, er, er, er, er, er, er, er, er, er, er, er, er, er, er  	// 240 - 255
	};

	public static byte decodeLoHex(byte val) {
		return loHexTable[val];
	}

}
