package org.verapdf.tools;

import org.apache.log4j.Logger;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

/**
 * @author Maksim Bezrukov
 */
public class TypeConverter {
	private static final Logger LOGGER = Logger.getLogger(TypeConverter.class);

	public static double[] getRealArray(COSObject array, int estimatedSize, String arrayName) {
		if (arrayName == null) {
			throw new IllegalArgumentException("Array object can not be null");
		}

		if (array != null && array.getType() == COSObjType.COS_ARRAY) {
			int size = array.size();

			if (size != estimatedSize) {
				LOGGER.debug(arrayName + " array doesn't consist of " + estimatedSize + " elements");
			}

			double[] res = new double[size];
			for (int i = 0; i < size; ++i) {
				COSObject number = array.at(i);
				if (number == null || number.getReal() == null) {
					LOGGER.debug(arrayName + " array contains non number value");
					return null;
				} else {
					res[i] = number.getReal();
				}
			}
			return res;
		}
		return null;
	}
}
