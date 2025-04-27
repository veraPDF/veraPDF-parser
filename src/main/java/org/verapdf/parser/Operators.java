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
package org.verapdf.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Timur Kamalov
 */
public class Operators {

    public static final String B_CLOSEPATH_FILL_STROKE = "b";
    public static final String B_FILL_STROKE = "B";
    public static final String BMC = "BMC";
    public static final String BDC = "BDC";
    public static final String B_STAR_CLOSEPATH_EOFILL_STROKE = "b*";
    public static final String B_STAR_EOFILL_STROKE = "B*";
    public static final String BX = "BX";
    public static final String C_CURVE_TO = "c";
    public static final String CM_CONCAT = "cm";
    public static final String D_SET_DASH = "d";
    public static final String DO = "Do";
    public static final String EMC = "EMC";
    public static final String EX = "EX";
    public static final String F_FILL = "f";
    public static final String F_FILL_OBSOLETE = "F";
    public static final String F_STAR_FILL = "f*";
    public static final String GS = "gs";
    public static final String H_CLOSEPATH = "h";
    public static final String I_SETFLAT = "i";
    public static final String J_LINE_CAP = "J";
    public static final String J_LINE_JOIN = "j";
    public static final String L_LINE_TO = "l";
    public static final String M_MITER_LIMIT = "M";
    public static final String M_MOVE_TO = "m";
    public static final String MP = "MP";
    public static final String DP = "DP";
    public static final String N = "n";
    public static final String Q_GRESTORE = "Q";
    public static final String Q_GSAVE = "q";
    public static final String RE = "re";
    public static final String RI = "ri";
    public static final String S_CLOSE_STROKE = "s";
    public static final String S_STROKE = "S";
    public static final String SH = "sh";
    public static final String V = "v";
    public static final String W_CLIP = "W";
    public static final String W_LINE_WIDTH = "w";
    public static final String W_STAR_EOCLIP = "W*";
    public static final String Y = "y";
    public static final String CS_STROKE = "CS";
    public static final String CS_FILL = "cs";
    public static final String SCN_STROKE = "SCN";
    public static final String SCN_FILL = "scn";
    public static final String SC_STROKE = "SC";
    public static final String SC_FILL = "sc";
    public static final String G_STROKE = "G";
    public static final String G_FILL = "g";
    public static final String RG_STROKE = "RG";
    public static final String RG_FILL = "rg";
    public static final String K_STROKE = "K";
    public static final String K_FILL = "k";
    public static final String BI = "BI";
    public static final String ID = "ID";
    public static final String EI = "EI";
    public static final String ET = "ET";
    public static final String BT = "BT";
    public static final String TD_MOVE = "Td";
    public static final String TD_MOVE_SET_LEADING = "TD";
    public static final String TM = "Tm";
    public static final String T_STAR = "T*";
    public static final String TJ_SHOW = "Tj";
    public static final String TJ_SHOW_POS = "TJ";
    public static final String QUOTE = "\'";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String TC = "Tc";
    public static final String TW = "Tw";
    public static final String TZ = "Tz";
    public static final String TL = "TL";
    public static final String TF = "Tf";
    public static final String TR = "Tr";
    public static final String TS = "Ts";
    public static final String D0 = "d0";
    public static final String D1 = "d1";

    public static final Set<String> operators = new HashSet<>();

    static {

        operators.add(B_CLOSEPATH_FILL_STROKE);
        operators.add(B_FILL_STROKE);
        operators.add(BMC);
        operators.add(BDC);
        operators.add(B_STAR_CLOSEPATH_EOFILL_STROKE);
        operators.add(B_STAR_EOFILL_STROKE);
        operators.add(BX);
        operators.add(C_CURVE_TO);
        operators.add(CM_CONCAT);
        operators.add(D_SET_DASH);
        operators.add(DO);
        operators.add(EMC);
        operators.add(EX);
        operators.add(F_FILL);
        operators.add(F_FILL_OBSOLETE);
        operators.add(F_STAR_FILL);
        operators.add(GS);
        operators.add(H_CLOSEPATH);
        operators.add(I_SETFLAT);
        operators.add(J_LINE_CAP);
        operators.add(J_LINE_JOIN);
        operators.add(L_LINE_TO);
        operators.add(M_MITER_LIMIT);
        operators.add(M_MOVE_TO);
        operators.add(MP);
        operators.add(DP);
        operators.add(N);
        operators.add(Q_GRESTORE);
        operators.add(Q_GSAVE);
        operators.add(RE);
        operators.add(RI);
        operators.add(S_CLOSE_STROKE);
        operators.add(S_STROKE);
        operators.add(SH);
        operators.add(V);
        operators.add(W_CLIP);
        operators.add(W_LINE_WIDTH);
        operators.add(W_STAR_EOCLIP);
        operators.add(Y);
        operators.add(CS_STROKE);
        operators.add(CS_FILL);
        operators.add(SCN_STROKE);
        operators.add(SCN_FILL);
        operators.add(SC_STROKE);
        operators.add(SC_FILL);
        operators.add(G_STROKE);
        operators.add(G_FILL);
        operators.add(RG_STROKE);
        operators.add(RG_FILL);
        operators.add(K_STROKE);
        operators.add(K_FILL);
        operators.add(BI);
        operators.add(ID);
        operators.add(EI);
        operators.add(ET);
        operators.add(BT);
        operators.add(TD_MOVE);
        operators.add(TD_MOVE_SET_LEADING);
        operators.add(TM);
        operators.add(T_STAR);
        operators.add(TJ_SHOW);
        operators.add(TJ_SHOW_POS);
        operators.add(QUOTE);
        operators.add(DOUBLE_QUOTE);
        operators.add(TC);
        operators.add(TW);
        operators.add(TZ);
        operators.add(TL);
        operators.add(TF);
        operators.add(TR);
        operators.add(TS);
        operators.add(D0);
        operators.add(D1);
    }

    private Operators() {
        // Disable default constructor
    }
}
