package org.verapdf.pd.font.stdmetrics;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.parser.BaseParser;
import org.verapdf.parser.Token;

import java.io.IOException;

/**
 * @author Sergey Shemyakov
 */
public class AFMParser extends BaseParser {

    private static final String START_CHAR_METRICS_STRING = "StartCharMetrics";
    private String fontName;
    private int nGlyphs;

    public AFMParser(ASInputStream asInputStream, String fontName) throws IOException {
        super(asInputStream);
        this.fontName = fontName;
    }

    public StandardFontMetrics parse() throws IOException {
        StandardFontMetrics res = new StandardFontMetrics();
        this.initializeToken();
        this.findStartCharMetrics();
        this.nextToken();
        if (getToken().type == Token.Type.TT_INTEGER) {
            nGlyphs = (int) this.getToken().integer;
            for (int i = 0; i < nGlyphs; ++i) {
                readMetricsLine(res);
            }
        } else {    // Actually in this case we can try read until
            // EndCharMetrics, but we are sure that files are perfect.
            throw new IOException("Can't parse font metrics for predefined font "
                    + fontName);
        }
        return res;
    }

    private void findStartCharMetrics() throws IOException {
        do {
            nextToken();
        } while (this.getToken().type != Token.Type.TT_EOF &&
                !this.getToken().getValue().equals(START_CHAR_METRICS_STRING));
        if (this.getToken().type == Token.Type.TT_EOF) {
            throw new IOException("Can't parse font metrics for predefined font "
                    + fontName);
        }
    }

    private void readMetricsLine(StandardFontMetrics sfm) throws IOException {
        this.nextToken();   // C
        this.nextToken();   // character code
        this.nextToken();   // ;
        this.nextToken();   // WX
        this.nextToken();
        int width = (int) this.getToken().integer;
        this.nextToken();   // ;
        this.nextToken();   // N
        this.nextToken();
        sfm.putWidth(this.getToken().getValue(), width);
        for (int i = 0; i < 7; ++i) {   // finish reading line
            this.nextToken();
        }
    }
}
