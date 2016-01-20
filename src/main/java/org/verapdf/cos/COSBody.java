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

		/*
		COSObject value = null;
		//TODO : don't even think about leaving this nightmare in code
		//TODO : override hashCode in COSKey
		//COSObject value	= this.table.get(key);
		for (Map.Entry<COSKey, COSObject> entry : table.entrySet()) {
			if (entry.getKey().equals(key)) {
				value = entry.getValue();
				break;
			}
		}
		*/
		return value != null ? value : COSObject.getEmpty();
	}

	public void set(final COSKey key, final COSObject object) {
		table.put(key, object);
	}

}
