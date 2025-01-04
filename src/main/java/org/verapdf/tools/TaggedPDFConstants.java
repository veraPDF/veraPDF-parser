/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.tools;

/**
 * Class with constants for tagged pdf structure
 */
public final class TaggedPDFConstants {

    public static final String HN_REGEXP = "^H[1-9][0-9]*$";

    public static final String PDF_NAMESPACE = "http://iso.org/pdf/ssn";
    public static final String PDF2_NAMESPACE = "http://iso.org/pdf2/ssn";
    public static final String MATH_ML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";

    public static final String MATH_ML = "MathML";

    // Common standard structure types for PDF 1.7 and 2.0
    public static final String DOCUMENT = "Document";
    public static final String PART = "Part";
    public static final String DIV = "Div";
    public static final String CAPTION = "Caption";
    public static final String THEAD = "THead";
    public static final String TBODY = "TBody";
    public static final String TFOOT = "TFoot";
    public static final String H = "H";
    public static final String P = "P";
    public static final String L = "L";
    public static final String LI = "LI";
    public static final String LBL = "Lbl";
    public static final String LBODY = "LBody";
    public static final String TABLE = "Table";
    public static final String TR = "TR";
    public static final String TH = "TH";
    public static final String TD = "TD";
    public static final String SPAN = "Span";
    public static final String LINK = "Link";
    public static final String ANNOT = "Annot";
    public static final String RUBY = "Ruby";
    public static final String WARICHU = "Warichu";
    public static final String FIGURE = "Figure";
    public static final String FORMULA = "Formula";
    public static final String FORM = "Form";
    public static final String RB = "RB";
    public static final String RT = "RT";
    public static final String RP = "RP";
    public static final String WT = "WT";
    public static final String WP = "WP";

    // Standard structure types present in 1.7
    public static final String ART = "Art";
    public static final String SECT = "Sect";
    public static final String BLOCK_QUOTE = "BlockQuote";
    public static final String TOC = "TOC";
    public static final String TOCI = "TOCI";
    public static final String INDEX = "Index";
    public static final String NON_STRUCT = "NonStruct";
    public static final String PRIVATE = "Private";
    public static final String QUOTE = "Quote";
    public static final String NOTE = "Note";
    public static final String REFERENCE = "Reference";
    public static final String BIB_ENTRY = "BibEntry";
    public static final String CODE = "Code";
    public static final String H1 = "H1";
    public static final String H2 = "H2";
    public static final String H3 = "H3";
    public static final String H4 = "H4";
    public static final String H5 = "H5";
    public static final String H6 = "H6";

    // Standard structure types present in 2.0
    public static final String DOCUMENT_FRAGMENT = "DocumentFragment";
    public static final String ASIDE = "Aside";
    public static final String TITLE = "Title";
    public static final String FENOTE = "FENote";
    public static final String SUB = "Sub";
    public static final String EM = "Em";
    public static final String STRONG = "Strong";
    public static final String ARTIFACT = "Artifact";

    private TaggedPDFConstants() {
        //disable default constructor
    }
}
