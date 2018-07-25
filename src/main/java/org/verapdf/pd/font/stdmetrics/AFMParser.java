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
package org.verapdf.pd.font.stdmetrics;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.parser.BaseParser;
import org.verapdf.parser.Token;

import java.io.IOException;

/**
 * Class that is used to parse Adobe Font Metrics files.
 *
 * @author Sergey Shemyakov
 */
public class AFMParser extends BaseParser {

    private static final String START_CHAR_METRICS_STRING = "StartCharMetrics";
    private static final String FONT_NAME_STRING = "FontName";
    private static final String FAMILY_NAME_STRING = "FamilyName";
    private static final String FONT_BBOX_STRING = "FontBBox";
    private static final String ENCODING_SCHEME_STRING = "EncodingScheme";
    private static final String CHARSET_STRING = "CharacterSet";
    private static final String CAP_HEIGHT_STRING = "CapHeight";
    private static final String X_HEIGHT_STRING = "XHeight";
    private static final String ASCEND_STRING = "Ascender";
    private static final String DESCEND_STRING = "Descender";
    private static final String ITALIC_ANGLE_STRING = "ItalicAngle";

    private String fontName;

    public AFMParser(ASInputStream asInputStream, String fontName) throws IOException {
        super(asInputStream);
        this.fontName = fontName;
    }

    public StandardFontMetrics parse() throws IOException {
        StandardFontMetrics res = new StandardFontMetrics();
        try {
            initializeToken();
            while (this.getToken().type != Token.Type.TT_EOF) {
                this.nextToken();
                this.processToken(res);
            }
        } finally {
            this.source.close();    // We close stream after first reading attempt
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

    private void processToken(StandardFontMetrics res) throws IOException {
        if (getToken().type == Token.Type.TT_KEYWORD) {
            this.skipSpaces();
            switch (getToken().getValue()) {
                case START_CHAR_METRICS_STRING:
                    this.nextToken();
                    if (getToken().type == Token.Type.TT_INTEGER) {
                        int nGlyphs = (int) this.getToken().integer;
                        for (int i = 0; i < nGlyphs; ++i) {
                            readMetricsLine(res);
                        }
                    } else {    // Actually in this case we can try read until
                        // EndCharMetrics, but we are sure that files are perfect.
                        throw new IOException("Can't parse font metrics for predefined font "
                                + fontName);
                    }
                    break;
                case FONT_NAME_STRING:
                    res.setFontName(this.getLine());
                    break;
                case FAMILY_NAME_STRING:
                    res.setFamilyName(this.getLine());
                    break;
                case FONT_BBOX_STRING:
                    double[] bbox = new double[4];
                    for (int i = 0; i < 4; ++i) {
                        this.nextToken();
                        if (getToken().type == Token.Type.TT_INTEGER ||
                                getToken().type == Token.Type.TT_REAL) {
                            bbox[i] = getToken().real;
                        } else {
                            throw new IOException("Font BBox in AFM file for " + fontName +
                                    " doesn't contain 4 entries");
                        }
                    }
                    res.setFontBBox(bbox);
                    break;
                case ENCODING_SCHEME_STRING:
                    res.setEncodingScheme(this.getLine());
                    break;
                case CHARSET_STRING:
                    res.setCharSet(this.getLine());
                    break;
                case CAP_HEIGHT_STRING:
                    res.setCapHeight(getNextDoubleWithCheck("Cap height"));
                    break;
                case X_HEIGHT_STRING:
                    res.setXHeight(getNextDoubleWithCheck("XHeight"));
                    break;
                case ASCEND_STRING:
                    res.setAscend(getNextDoubleWithCheck("Ascender"));
                    break;
                case DESCEND_STRING:
                    res.setDescend(getNextDoubleWithCheck("Descender"));
                    break;
                case ITALIC_ANGLE_STRING:
                    res.setItalicAngle(getNextDoubleWithCheck("Italic angle"));
                    break;
            }
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

    private double getNextDoubleWithCheck(String errorDescription) throws IOException {
        this.nextToken();
        if (getToken().type == Token.Type.TT_INTEGER ||
                getToken().type == Token.Type.TT_REAL) {
            return getToken().real;
        } else {
            throw new IOException(errorDescription + " entry in AFM file for "
                    + fontName + "is not a number");
        }
    }
}
