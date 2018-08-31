/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.tools;

import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class TypeConverter {
	private static final Logger LOGGER = Logger.getLogger(TypeConverter.class.getCanonicalName());
	private static final String INVALID_DATE_LOG_BASE =
			"Parsed string is not complies pdf date format. ";

	public static Calendar parseDate(String toParse) {
		if (toParse != null
				&& toParse.startsWith("D:")) {

			if (toParse.endsWith("'")) {
				if (toParse.endsWith("''")) {
					return getErrorDateFormat("Trailing apostrophe duplication");
				} else {
					toParse = toParse.substring(0, toParse.length() - 1);
				}
			}

			if (!isDigits(toParse, 2, 4)) {
				return getErrorDateFormat("Incorrect year data.");
			}
			int year = Integer.parseInt(toParse.substring(2, 6));
			int month = 1;
			int day = 1;
			int hour = 0;
			int minutes = 0;
			int seconds = 0;
			String sign = "Z";
			int timeZoneHours = 0;
			int timeZoneMins = 0;

			int length = toParse.length();

			if (length > 6) {
				if (isDigits(toParse, 6, 2)) {
					month = Integer.parseInt(toParse.substring(6, 8)) - 1;
				} else {
					return getErrorDateFormat("Incorrect month data.");
				}
			}

			if (length > 8) {
				if (isDigits(toParse, 8, 2)) {
					day = Integer.parseInt(toParse.substring(8, 10));
				} else {
					return getErrorDateFormat("Incorrect day data.");
				}
			}
			if (length > 10) {
				if (isDigits(toParse, 10, 2)) {
					hour = Integer.parseInt(toParse.substring(10, 12));
				} else {
					return getErrorDateFormat("Incorrect hour data.");
				}
			}
			if (length > 12) {
				if (isDigits(toParse, 12, 2)) {
					minutes = Integer.parseInt(toParse.substring(12, 14));
				} else {
					return getErrorDateFormat("Incorrect minutes data.");
				}
			}
			if (length > 14) {
				if (isDigits(toParse, 14, 2)) {
					seconds = Integer.parseInt(toParse.substring(14, 16));
				} else {
					return getErrorDateFormat("Incorrect seconds data.");
				}
			}

			if (length > 16) {
				sign = toParse.substring(16, 17);
				if (!sign.matches("^[Z+-]$")) {
					return getErrorDateFormat("Incorrect time zone beginning.");
				}
			}

			if (length > 17) {
				if (isDigits(toParse, 17, 2)) {
					timeZoneHours = Integer.parseInt(toParse.substring(17, 19));
				} else {
					return getErrorDateFormat("Incorrect time zone hours data.");
				}
			}

			if (length > 19) {
				if (!"'".equals(toParse.substring(19, 20))) {
					return getErrorDateFormat("Missing apostrophe delimiter.");
				}
			}

			if (length > 20) {
				if (isDigits(toParse, 20, 2)) {
					timeZoneMins = Integer.parseInt(toParse.substring(20, 22));
				} else {
					return getErrorDateFormat("Incorrect time zone minutes data.");
				}
			}

			if (length > 22) {
				return getErrorDateFormat("Incorrect ending.");
			}

			TimeZone zone;
			if (!sign.equals("Z")) {
				zone = TimeZone.getTimeZone(String.format(
						"GMT%s%d:%02d", sign, timeZoneHours, timeZoneMins));
			} else if (timeZoneHours == 0 && timeZoneMins == 0) {
				zone = TimeZone.getTimeZone("GMT");
			} else {
				return getErrorDateFormat("Incorrect time zone data.");
			}
			Calendar res = new GregorianCalendar(zone);
			res.set(year, month, day, hour, minutes, seconds);
			res.set(Calendar.MILLISECOND, 0);
			return res;
		}

		return getErrorDateFormat("String is null or has incorrect beginning.");
	}

	private static Calendar getErrorDateFormat(String logMessageEnd) {
		LOGGER.log(Level.FINE, INVALID_DATE_LOG_BASE + logMessageEnd);
		return null;
	}

	public static String getPDFDate(Calendar date) {
		int year = date.get(Calendar.YEAR);
		int month = date.get(Calendar.MONTH) + 1;
		int day = date.get(Calendar.DAY_OF_MONTH);
		int hour = date.get(Calendar.HOUR_OF_DAY);
		int min = date.get(Calendar.MINUTE);
		int sec = date.get(Calendar.SECOND);
		SimpleDateFormat sdf = new SimpleDateFormat("Z");
		sdf.setTimeZone(date.getTimeZone());
		String tz = sdf.format(new Date());
		return String.format("D:%04d%02d%02d%02d%02d%02d%s'%s", year, month, day, hour, min, sec, tz.substring(0, 3), tz.substring(3, 5));
	}

	private static boolean isDigits(String toCheck, int offset, int length) {
		int end = offset + length;
		if (end > toCheck.length()) {
			return false;
		}
		for (int i = offset; i < end; ++i) {
			if (!Character.isDigit(toCheck.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static double[] getRealArray(COSObject array, int estimatedSize, String arrayName) {
		return getRealArray(array, estimatedSize, arrayName, true);
	}

	public static double[] getRealArray(COSObject array, String arrayName) {
		return getRealArray(array, 0, arrayName, false);
	}

	private static double[] getRealArray(COSObject array, int estimatedSize, String arrayName, boolean checkSize) {
		if (arrayName == null) {
			throw new IllegalArgumentException("Array object can not be null");
		}

		if (array != null && array.getType() == COSObjType.COS_ARRAY) {
			int size = array.size().intValue();

			if (checkSize && size != estimatedSize) {
				LOGGER.log(Level.FINE, arrayName + " array doesn't consist of " + estimatedSize + " elements");
			}

			double[] res = new double[size];
			for (int i = 0; i < size; ++i) {
				COSObject number = array.at(i);
				if (number == null || number.getReal() == null) {
					LOGGER.log(Level.FINE, arrayName + " array contains non number value");
					return null;
				}
				res[i] = number.getReal().doubleValue();
			}
			return res;
		}
		return null;
	}
}
