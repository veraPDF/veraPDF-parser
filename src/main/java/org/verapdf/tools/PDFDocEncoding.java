package org.verapdf.tools;

/**
 * Class handles PDFDoc string encoding.
 *
 * @author Sergey Shemyakov
 */
public class PDFDocEncoding {

    private static final int UNKNOWN = '\uFFFD';
    private static int[] encoding = new int[256];

    static {
        for (int i = 0; i < 256; ++i) {
            encoding[i] = i;
        }

        encoding[0x18] = '\u02D8'; // BREVE
        encoding[0x19] = '\u02C7'; // CARON
        encoding[0x1A] = '\u02C6'; // MODIFIER LETTER CIRCUMFLEX ACCENT
        encoding[0x1B] = '\u02D9'; // DOT ABOVE
        encoding[0x1C] = '\u02DD'; // DOUBLE ACUTE ACCENT
        encoding[0x1D] = '\u02DB'; // OGONEK
        encoding[0x1E] = '\u02DA'; // RING ABOVE
        encoding[0x1F] = '\u02DC'; // SMALL TILDE

        encoding[0x7F] = UNKNOWN; // undefined
        encoding[0x80] = '\u2022'; // BULLET
        encoding[0x81] = '\u2020'; // DAGGER
        encoding[0x82] = '\u2021'; // DOUBLE DAGGER
        encoding[0x83] = '\u2026'; // HORIZONTAL ELLIPSIS
        encoding[0x84] = '\u2014'; // EM DASH
        encoding[0x85] = '\u2013'; // EN DASH
        encoding[0x86] = '\u0192'; // LATIN SMALL LETTER SCRIPT F
        encoding[0x87] = '\u2044'; // FRACTION SLASH
        encoding[0x88] = '\u2039'; // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        encoding[0x89] = '\u203A'; // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        encoding[0x8A] = '\u2212'; // MINUS SIGN
        encoding[0x8B] = '\u2030'; // PER MILLE SIGN
        encoding[0x8C] = '\u201E'; // DOUBLE LOW-9 QUOTATION MARK
        encoding[0x8D] = '\u201C'; // LEFT DOUBLE QUOTATION MARK
        encoding[0x8E] = '\u201D'; // RIGHT DOUBLE QUOTATION MARK
        encoding[0x8F] = '\u2018'; // LEFT SINGLE QUOTATION MARK
        encoding[0x90] = '\u2019'; // RIGHT SINGLE QUOTATION MARK
        encoding[0x91] = '\u201A'; // SINGLE LOW-9 QUOTATION MARK
        encoding[0x92] = '\u2122'; // TRADE MARK SIGN
        encoding[0x93] = '\uFB01'; // LATIN SMALL LIGATURE FI
        encoding[0x94] = '\uFB02'; // LATIN SMALL LIGATURE FL
        encoding[0x95] = '\u0141'; // LATIN CAPITAL LETTER L WITH STROKE
        encoding[0x96] = '\u0152'; // LATIN CAPITAL LIGATURE OE
        encoding[0x97] = '\u0160'; // LATIN CAPITAL LETTER S WITH CARON
        encoding[0x98] = '\u0178'; // LATIN CAPITAL LETTER Y WITH DIAERESIS
        encoding[0x99] = '\u017D'; // LATIN CAPITAL LETTER Z WITH CARON
        encoding[0x9A] = '\u0131'; // LATIN SMALL LETTER DOTLESS I
        encoding[0x9B] = '\u0142'; // LATIN SMALL LETTER L WITH STROKE
        encoding[0x9C] = '\u0153'; // LATIN SMALL LIGATURE OE
        encoding[0x9D] = '\u0161'; // LATIN SMALL LETTER S WITH CARON
        encoding[0x9E] = '\u017E'; // LATIN SMALL LETTER Z WITH CARON
        encoding[0x9F] = UNKNOWN; // undefined
        encoding[0xA0] = '\u20AC'; // EURO SIGN
    }

    private PDFDocEncoding() {
    }

    /**
     * Applies PDF doc encoding to given byte array.
     *
     * @param bytes to be PDF doc encoded.
     * @return String that containes PDF doc encoded bytes.
     */
    public static String getStringFromBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if ((b & 0xFF) >= encoding.length) {
                sb.append(UNKNOWN);
            } else {
                sb.append((char) encoding[b & 0xFF]);
            }
        }
        return sb.toString();
    }
}
