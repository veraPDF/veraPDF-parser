package org.verapdf.parser;

import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSTrailer;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.io.COSXRefTableReader;
import org.verapdf.io.IReader;

import java.io.IOException;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public abstract class XRefReader implements IReader {

	private COSXRefTableReader xref;

	//CONSTRUCTORS
	public XRefReader() throws IOException {
		this.xref = new COSXRefTableReader();
	}

	public XRefReader(final List<COSXRefInfo> infos) throws IOException {
		this.xref = new COSXRefTableReader(infos);
	}

	public XRefReader(final COSXRefInfo info) throws IOException {
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

	public COSTrailer getFirstTrailer() {
		return this.xref.getFirstTrailer();
	}

	public COSTrailer getLastTrailer() {
		return this.xref.getLastTrailer();
	}

	//PROTECTED METHODS
	protected void setXRefInfo(final List<COSXRefInfo> infos) {
		this.xref.set(infos);
	}

	protected void setXRefInfo(final COSXRefInfo info) {
		this.xref.set(info);
	}

	@Override
	public Long getOffset(final COSKey key) {
		return this.xref.getOffset(key);
	}

	protected boolean containsKey(final COSKey key) {
		return this.xref.containsKey(key);
	}

}
