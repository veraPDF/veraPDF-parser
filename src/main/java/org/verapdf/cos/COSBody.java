package org.verapdf.cos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class COSBody {

	private Map<COSKey, COSObject> table;

	public COSBody() {
		this.table = new HashMap<>();
	}

	public List<COSObject> getAll() {
		List<COSObject> result = new ArrayList<>();
		for (Map.Entry<COSKey, COSObject> entry : table.entrySet()) {
			COSObject value = entry.getValue();
			if (value != null) {
				result.add(value);
			}
		}
		return result;
	}

	public COSObject get(final COSKey key) {
		COSObject value = this.table.get(key);
		return value != null ? value : COSObject.getEmpty();
	}

	public void set(final COSKey key, final COSObject object) {
		table.put(key, object);
	}

	public COSKey getKeyForObject(COSObject obj) {
		if (obj.isIndirect()) {
			return obj.getObjectKey();
		} else {
			for (COSKey key : this.table.keySet()) {
				if (this.table.get(key) == obj) {
                    return key;
                }
			}
			return null;
		}
	}
}
