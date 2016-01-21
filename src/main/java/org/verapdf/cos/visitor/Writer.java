package org.verapdf.cos.visitor;

import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSXRefInfo;
import org.verapdf.io.InternalOutputStream;

import java.util.List;

/**
 * @author Timur Kamalov
 */
public class Writer implements IVisitor {

	protected InternalOutputStream os;

	protected COSXRefInfo info;

	protected COSDocument document;

	protected List<COSKey> toWrite;
	protected List<COSKey> written;

}
