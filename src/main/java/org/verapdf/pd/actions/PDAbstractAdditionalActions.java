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
package org.verapdf.pd.actions;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDObject;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * @author Maksim Bezrukov
 */
public abstract class PDAbstractAdditionalActions extends PDObject {

    private static final Set<ASAtom> permittedActionNames;

    static {
        permittedActionNames = new HashSet<>();
        permittedActionNames.add(ASAtom.E);
        permittedActionNames.add(ASAtom.X);
        permittedActionNames.add(ASAtom.D);
        permittedActionNames.add(ASAtom.U);
        permittedActionNames.add(ASAtom.FOCUS_ABBREVIATION);
        permittedActionNames.add(ASAtom.BL_FOCUS);
    }

    protected PDAbstractAdditionalActions(COSObject obj) {
        super(obj);
    }

    protected PDAction getAction(ASAtom key) {
        COSObject obj = getKey(key);
        if (obj != null && obj.getType() == COSObjType.COS_DICT) {
            return new PDAction(obj);
        }
        return null;
    }

    public ASAtom[] getActionNames() {
        return null;
    }

    public List<PDAction> getActions() {
        ASAtom[] actionNames = getActionNames();
        if (actionNames == null) {
            return Collections.emptyList();
        }
        List<PDAction> actions = new ArrayList<>(actionNames.length);
        PDAction action;
        for (ASAtom name : actionNames) {
            action = getAction(name);
            if (name != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    // return true, if dictionary contains keys not from the following list: E, X, D, U, Fo, and Bl.
    public boolean containsOtherKeys() {
        if (!this.getObject().empty()) {
            Set<ASAtom> names = this.getObject().getDirectBase().getKeySet();
            for (ASAtom name : names) {
                if (!permittedActionNames.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

}
