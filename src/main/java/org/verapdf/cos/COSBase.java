package org.verapdf.cos;

import org.verapdf.as.ASAtom;

/**
 * @author Timur Kamalov
 */
public abstract class COSBase {

	private int count;

	public COSBase() {
		this.count = 0;
	}

	// DICTIONARIES
	public abstract boolean setKey(final ASAtom key, final COSObject value);

	// ARRAYS
	public abstract boolean add(final COSObject value);

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
