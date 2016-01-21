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
		this.copy = COSBoolean.construct(obj.get());
	}

	public void visitFromInteger(COSInteger obj) {
		this.copy = COSInteger.construct(obj.get());
	}

	public void visitFromReal(COSReal obj) {
		this.copy = COSReal.construct(obj.get());
	}

	public void visitFromString(COSString obj) {
		this.copy = COSString.construct(obj.get(), obj.isHexadecimal());
	}

	public void visitFromName(COSName obj) {
		this.copy = COSName.construct(obj.get());
	}

	public void visitFromArray(COSArray obj) {
		this.copy = COSArray.construct();

		COSObject element = new COSObject();
		COSCopier copier = new COSCopier(element);

		for (int i = 0; i < obj.size(); ++i) {
			obj.at(i).accept(copier);
			this.copy.add(element);
		}
	}

	public void visitFromDictionary(COSDictionary obj) {
		this.copy = COSDictionary.construct();

		COSObject element = new COSObject();
		COSCopier copier = new COSCopier(element);

		for (Map.Entry<ASAtom, COSObject> entry : obj.getEntrySet()) {
			entry.getValue().accept(copier);
			this.copy.setKey(entry.getKey(), element);
		}
	}

	public void visitFromNull(COSNull obj) {
		//TODO
		this.copy = obj;
	}

	public void visitFromIndirect(COSIndirect obj) {
		//TODO
	}
}
