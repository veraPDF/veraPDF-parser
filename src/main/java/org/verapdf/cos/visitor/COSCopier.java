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
