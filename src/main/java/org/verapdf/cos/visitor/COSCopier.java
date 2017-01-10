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
package org.verapdf.cos.visitor;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;

import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class COSCopier implements IVisitor {

	private COSObject copy;

	public COSCopier(COSObject copy) {
		this.copy = copy;
	}

	public void visitFromBoolean(COSBoolean obj) {
		this.copy.assign(COSBoolean.construct(obj.get()));
	}

	public void visitFromInteger(COSInteger obj) {
		this.copy.assign(COSInteger.construct(obj.get()));
	}

	public void visitFromReal(COSReal obj) {
		this.copy.assign(COSReal.construct(obj.get()));
	}

	public void visitFromString(COSString obj) {
		this.copy.assign(COSString.construct(obj.get(), obj.isHexadecimal()));
	}

	public void visitFromName(COSName obj) {
		this.copy.assign(COSName.construct(obj.get()));
	}

	public void visitFromArray(COSArray obj) {
		this.copy.assign(COSArray.construct());

		for (int i = 0; i < obj.size(); ++i) {
			COSObject element = new COSObject();
			COSCopier copier = new COSCopier(element);
			obj.at(i).accept(copier);
			this.copy.add(element);
		}
	}

	public void visitFromDictionary(COSDictionary obj) {
		this.copy.assign(COSDictionary.construct());

		for (Map.Entry<ASAtom, COSObject> entry : obj.getEntrySet()) {
			COSObject element = new COSObject();
			COSCopier copier = new COSCopier(element);
			entry.getValue().accept(copier);
			this.copy.setKey(entry.getKey(), element);
		}
	}

	public void visitFromStream(COSStream obj) {
		visitFromDictionary(obj);
		this.copy.assign(COSStream.construct((COSDictionary) this.copy.get(), obj.getData(), obj.getFilterFlags()));
	}

	public void visitFromNull(COSNull obj) {
		//TODO : make singleton
		this.copy.assign(COSNull.construct());
	}

	public void visitFromIndirect(COSIndirect obj) {
		try {
			this.copy.set(obj);
		} catch (Exception e) {
			//TODO : throw
			e.printStackTrace();
		}
	}
}
