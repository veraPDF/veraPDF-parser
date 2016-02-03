package org.verapdf.cos.visitor;

import org.verapdf.cos.*;

/**
 * @author Timur Kamalov
 */
public interface IVisitor {

	void visitFromBoolean(final COSBoolean obj);
	void visitFromInteger(final COSInteger obj);
	void visitFromReal(final COSReal obj);
	void visitFromString(final COSString obj);
	void visitFromName(final COSName obj);
	void visitFromArray(final COSArray obj);
	void visitFromDictionary(final COSDictionary obj);
	void visitFromStream(final COSStream obj);
	void visitFromNull(final COSNull obj);
	void visitFromIndirect(final COSIndirect obj);

}
