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

	public static Calendar parseDate(String toParse) {
		if (toParse != null
				&& toParse.startsWith("D:")
				&& isDigits(toParse, 2, 4)) {
			int year = Integer.parseInt(toParse.substring(2, 6));
			int month = 1;
			int day = 1;
			int hour = 0;
			int minutes = 0;
			int seconds = 0;
			String sign = "Z";
			int timeZoneHours = 0;
			int timeZoneMins = 0;
			boolean isCorrect = true;

			int length = toParse.length();

			if (length > 6) {
				if (isDigits(toParse, 6, 2)) {
					month = Integer.parseInt(toParse.substring(6, 8)) - 1;
				} else {
					isCorrect = false;
				}
			}

			if (length > 8) {
				if (isDigits(toParse, 8, 2)) {
					day = Integer.parseInt(toParse.substring(8, 10));
				} else {
					isCorrect = false;
				}
			}
			if (length > 10) {
				if (isDigits(toParse, 10, 2)) {
					hour = Integer.parseInt(toParse.substring(10, 12));
				} else {
					isCorrect = false;
				}
			}
			if (length > 12) {
				if (isDigits(toParse, 12, 2)) {
					minutes = Integer.parseInt(toParse.substring(12, 14));
				} else {
					isCorrect = false;
				}
			}
			if (length > 14) {
				if (isDigits(toParse, 14, 2)) {
					seconds = Integer.parseInt(toParse.substring(14, 16));
				} else {
					isCorrect = false;
				}
			}

			if (length > 16) {
				sign = toParse.substring(16, 17);
				if (!sign.matches("^[Z+-]$")) {
					isCorrect = false;
				}
				if ("Z".equals(sign) && length > 17) {
					isCorrect = false;
				}
			}

			if (length > 17) {
				if (isDigits(toParse, 17, 2)) {
					timeZoneHours = Integer.parseInt(toParse.substring(17, 19));
				} else {
					isCorrect = false;
				}
			}

			if (length > 19) {
				if (!"'".equals(toParse.substring(19, 20))) {
					isCorrect = false;
				}
			}

			if (length > 20) {
				if (isDigits(toParse, 20, 2)) {
					timeZoneMins = Integer.parseInt(toParse.substring(20, 22));
				} else {
					isCorrect = false;
				}
			}

			// date format from PDF 1.4 spec
			if (length > 22) {
				if (!"'".equals(toParse.substring(22, 23))) {
					isCorrect = false;
				}
			}

			if (length > 23) {
				isCorrect = false;
			}

			if (isCorrect) {
				TimeZone zone;
				if (!sign.equals("Z")) {
					String timeZoneMinsString =
							timeZoneMins < 10 ? "0" + Integer.toString(timeZoneMins)
									: Integer.toString(timeZoneMins);
					zone = TimeZone.getTimeZone("GMT" + sign + timeZoneHours + ":" + timeZoneMinsString);
				} else {
					zone = TimeZone.getTimeZone("GMT");
				}
				Calendar res = new GregorianCalendar(zone);
				res.set(year, month, day, hour, minutes, seconds);
				res.set(Calendar.MILLISECOND, 0);
				return res;
			}
		}

		LOGGER.log(Level.FINE, "Parsed string is not complies pdf date format");
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
		int maxOffset = Math.min(offset + length, toCheck.length());
		for (int i = offset; i < maxOffset; ++i) {
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
