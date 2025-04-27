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
package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.tools.StaticResources;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maxim Plushchov
 */
public class PDDestination extends PDObject {

	public static final Logger LOGGER = Logger.getLogger(PDDestination.class.getCanonicalName());

	public PDDestination(COSObject obj) {
		super(obj);
	}

	public Boolean getIsStructDestination() {
		COSObject destination = getObject();
		if (destination.getType() == COSObjType.COS_STRING) {
			PDNamesDictionary namesDictionary = StaticResources.getDocument().getCatalog().getNamesDictionary();
			if (namesDictionary == null) {
				return false;
			}
			PDNameTreeNode dests = namesDictionary.getDests();
			if (dests != null) {
				COSObject dest = dests.getObject(destination.getString());
				if (dest == null) {
					LOGGER.log(Level.WARNING, "Named destination " + destination.getString() +
							" not found in the Dests name tree in the Names dictionary");
					return false;
				}
				destination = dest;
			}
		} else if (destination.getType() == COSObjType.COS_NAME) {
			COSObject dests = StaticResources.getDocument().getCatalog().getDests();
			if (dests != null) {
				COSObject dest = dests.getKey(destination.getName());
				if (dest == null) {
					LOGGER.log(Level.WARNING, "Named destination " + destination.getName() +
							" not found in the Dests dictionary in the catalog");
					return false;
				}
				destination = dest;
			}
		}
		if (destination.getType() == COSObjType.COS_DICT) {
			return destination.knownKey(ASAtom.SD);
		}
		if (destination.getType() == COSObjType.COS_ARRAY && destination.size() > 0) {
			return destination.at(0).knownKey(ASAtom.S);
		}
		return false;
	}
}
