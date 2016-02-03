package org.verapdf.io;

import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSTrailer;
import org.verapdf.cos.xref.COSXRefInfo;

import java.util.List;

/**
 * @author Timur Kamalov
 */
public abstract class XRefReader implements IReader {

	private COSXRefTableReader xref;

	//CONSTRUCTORS
	public XRefReader() throws Exception {
		this.xref = new COSXRefTableReader();
	}

	public XRefReader(final List<COSXRefInfo> infos) throws Exception {
		this.xref = new COSXRefTableReader(infos);
	}

	public XRefReader(final COSXRefInfo info) throws Exception {
		this.xref = new COSXRefTableReader(info);
	}

	//PUBLIC METHODS
	public List<COSKey> getKeys() {
		return this.xref.getKeys();
	}

	public long getStartXRef() {
		return this.xref.getStartXRef();
	}

	public COSTrailer getTrailer() {
		return this.xref.getTrailer();
	}

	//PROTECTED METHODS
	protected void setXRefInfo(final List<COSXRefInfo> infos) {
		this.xref.set(infos);
	}

	protected void setXRefInfo(final COSXRefInfo info) {
		this.xref.set(info);
	}

	protected long getOffset(final COSKey key) {
		return this.xref.getOffset(key);
	}

}
