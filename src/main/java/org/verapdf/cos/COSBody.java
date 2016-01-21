package org.verapdf.cos;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class COSBody {

	private Map<COSKey, COSObject> table;

	public COSBody() {
		this.table = new HashMap<COSKey, COSObject>();
	}

	public COSObject get(final COSKey key) {
		COSObject value = this.table.get(key);
		return value != null ? value : COSObject.getEmpty();
	}

	public void set(final COSKey key, final COSObject object) {
		table.put(key, object);
	}

}
