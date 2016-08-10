package org.verapdf.cos;

/**
 * @author Timur Kamalov
 */
public enum COSObjType {

	COS_UNDEFINED,
	COS_NULL,
	COS_INTEGER,
	COS_REAL,
	COS_BOOLEAN,
	COS_NAME,
	COS_STRING,
	COS_DICT,
	COS_ARRAY,
	COS_STREAM;

	public boolean isNumber() {
		return this == COS_INTEGER || this == COS_REAL;
	}

	public boolean isDictionaryBased() {
		return this == COS_DICT || this == COS_STREAM;
	}
}
