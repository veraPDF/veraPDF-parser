package org.verapdf.cos;

import java.util.HashMap;

/**
 * @author Timur Kamalov
 */
public class COSBody {

	private HashMap<COSKey, COSObject> table;

	public COSBody() {
	}

	public COSObject get(final COSKey key) {
		return table.get(key);
	}

	public void set(final COSKey key, final COSObject object) {
		table.put(key, object);
	}


}
