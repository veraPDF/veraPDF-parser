package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public class COSObject {

	private COSBase base;

	public COSObject(COSBase base) {
		this.base = base;
	}

	public void set(COSBase base) {
		// TODO : maybe we need to use overriden equals method here instead of comparing links
		if (this.base == base) {
			return;
		}

		if (base != null) base.acquire();
		if (this.base != null) this.base.release();
		this.base = base;
	}

	public boolean empty() {
		return this.base == null;
	}

	public void clear() {
		this.set(null);
	}




}
