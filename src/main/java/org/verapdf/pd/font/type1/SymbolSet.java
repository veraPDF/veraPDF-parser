/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.font.type1;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Sergey Shemyakov
 */
public class SymbolSet {

    private static final Set<String> glyphNames = new TreeSet<>();

    static {
        glyphNames.add("Alpha");
        glyphNames.add("Beta");
        glyphNames.add("Chi");
        glyphNames.add("Delta");
        glyphNames.add("Epsilon");
        glyphNames.add("Eta");
        glyphNames.add("Euro");
        glyphNames.add("Gamma");
        glyphNames.add("Ifraktur");
        glyphNames.add("Iota");
        glyphNames.add("Kappa");
        glyphNames.add("Lambda");
        glyphNames.add("Mu");
        glyphNames.add("Nu");
        glyphNames.add("Omega");
        glyphNames.add("Omicron");
        glyphNames.add("Phi");
        glyphNames.add("Pi");
        glyphNames.add("Psi");
        glyphNames.add("Rfraktur");
        glyphNames.add("Rho");
        glyphNames.add("Sigma");
        glyphNames.add("Tau");
        glyphNames.add("Theta");
        glyphNames.add("Upsilon");
        glyphNames.add("Upsilon1");
        glyphNames.add("Xi");
        glyphNames.add("Zeta");
        glyphNames.add("aleph");
        glyphNames.add("alpha");
        glyphNames.add("ampersand");
        glyphNames.add("angle");
        glyphNames.add("angleleft");
        glyphNames.add("angleright");
        glyphNames.add("approxequal");
        glyphNames.add("arrowboth");
        glyphNames.add("arrowdblboth");
        glyphNames.add("arrowdbldown");
        glyphNames.add("arrowdblleft");
        glyphNames.add("arrowdblright");
        glyphNames.add("arrowdblup");
        glyphNames.add("arrowdown");
        glyphNames.add("arrowhorizex");
        glyphNames.add("arrowleft");
        glyphNames.add("arrowright");
        glyphNames.add("arrowup");
        glyphNames.add("arrowvertex");
        glyphNames.add("asteriskmath");
        glyphNames.add("bar");
        glyphNames.add("beta");
        glyphNames.add("braceleft");
        glyphNames.add("braceright");
        glyphNames.add("bracelefttp");
        glyphNames.add("braceleftmid");
        glyphNames.add("braceleftbt");
        glyphNames.add("bracerighttp");
        glyphNames.add("bracerightmid");
        glyphNames.add("bracerightbt");
        glyphNames.add("braceex");
        glyphNames.add("bracketleft");
        glyphNames.add("bracketright");
        glyphNames.add("bracketlefttp");
        glyphNames.add("bracketleftex");
        glyphNames.add("bracketleftbt");
        glyphNames.add("bracketrighttp");
        glyphNames.add("bracketrightex");
        glyphNames.add("bracketrightbt");
        glyphNames.add("bullet");
        glyphNames.add("carriagereturn");
        glyphNames.add("chi");
        glyphNames.add("circlemultiply");
        glyphNames.add("circleplus");
        glyphNames.add("club");
        glyphNames.add("colon");
        glyphNames.add("comma");
        glyphNames.add("congruent");
        glyphNames.add("copyrightsans");
        glyphNames.add("copyrightserif");
        glyphNames.add("degree");
        glyphNames.add("delta");
        glyphNames.add("diamond");
        glyphNames.add("divide");
        glyphNames.add("dotmath");
        glyphNames.add("eight");
        glyphNames.add("element");
        glyphNames.add("ellipsis");
        glyphNames.add("emptyset");
        glyphNames.add("epsilon");
        glyphNames.add("equal");
        glyphNames.add("equivalence");
        glyphNames.add("eta");
        glyphNames.add("exclam");
        glyphNames.add("existential");
        glyphNames.add("five");
        glyphNames.add("florin");
        glyphNames.add("four");
        glyphNames.add("fraction");
        glyphNames.add("gamma");
        glyphNames.add("gradient");
        glyphNames.add("greater");
        glyphNames.add("greaterequal");
        glyphNames.add("heart");
        glyphNames.add("infinity");
        glyphNames.add("integral");
        glyphNames.add("integraltp");
        glyphNames.add("integralex");
        glyphNames.add("integralbt");
        glyphNames.add("intersection");
        glyphNames.add("iota");
        glyphNames.add("kappa");
        glyphNames.add("lambda");
        glyphNames.add("less");
        glyphNames.add("lessequal");
        glyphNames.add("logicaland");
        glyphNames.add("logicalnot");
        glyphNames.add("logicalor");
        glyphNames.add("lozenge");
        glyphNames.add("minus");
        glyphNames.add("minute");
        glyphNames.add("mu");
        glyphNames.add("multiply");
        glyphNames.add("nine");
        glyphNames.add("notelement");
        glyphNames.add("notequal");
        glyphNames.add("notsubset");
        glyphNames.add("nu");
        glyphNames.add("numbersign");
        glyphNames.add("omega");
        glyphNames.add("omega1");
        glyphNames.add("omicron");
        glyphNames.add("one");
        glyphNames.add("parenleft");
        glyphNames.add("parenright");
        glyphNames.add("parenlefttp");
        glyphNames.add("parenleftex");
        glyphNames.add("parenleftbt");
        glyphNames.add("parenrighttp");
        glyphNames.add("parenrightex");
        glyphNames.add("parenrightbt");
        glyphNames.add("partialdiff");
        glyphNames.add("percent");
        glyphNames.add("period");
        glyphNames.add("perpendicular");
        glyphNames.add("phi");
        glyphNames.add("phi1");
        glyphNames.add("pi");
        glyphNames.add("plus");
        glyphNames.add("plusminus");
        glyphNames.add("product");
        glyphNames.add("propersubset");
        glyphNames.add("propersuperset");
        glyphNames.add("proportional");
        glyphNames.add("psi");
        glyphNames.add("question");
        glyphNames.add("radical");
        glyphNames.add("radicalex");
        glyphNames.add("reflexsubset");
        glyphNames.add("reflexsuperset");
        glyphNames.add("registersans");
        glyphNames.add("registerserif");
        glyphNames.add("rho");
        glyphNames.add("second");
        glyphNames.add("semicolon");
        glyphNames.add("seven");
        glyphNames.add("sigma");
        glyphNames.add("sigma1");
        glyphNames.add("similar");
        glyphNames.add("six");
        glyphNames.add("slash");
        glyphNames.add("space");
        glyphNames.add("spade");
        glyphNames.add("suchthat");
        glyphNames.add("summation");
        glyphNames.add("tau");
        glyphNames.add("therefore");
        glyphNames.add("theta");
        glyphNames.add("theta1");
        glyphNames.add("three");
        glyphNames.add("trademarksans");
        glyphNames.add("trademarkserif");
        glyphNames.add("two");
        glyphNames.add("underscore");
        glyphNames.add("union");
        glyphNames.add("universal");
        glyphNames.add("upsilon");
        glyphNames.add("weierstrass");
        glyphNames.add("xi");
        glyphNames.add("zero");
        glyphNames.add("zeta");
    }

    public static boolean hasGlyphName(String glyphName) {
        return glyphNames.contains(glyphName);
    }
}
