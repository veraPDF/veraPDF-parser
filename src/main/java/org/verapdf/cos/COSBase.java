package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public abstract class COSBase {

	private int count;

	public COSBase() {
		this.count = 0;
	}

	public abstract COSObjType getType();

	public abstract COSKey getKey();

	public abstract COSObject getObject();

	//TODO : seems this code is related to memory management. Not required in java
	public void acquire() {
		++this.count;
	}

	public void release() {
		if (--this.count == 0) {
			//delete this object
		}
	}

}
