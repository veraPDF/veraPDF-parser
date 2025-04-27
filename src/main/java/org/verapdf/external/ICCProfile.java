/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.external;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDMetadata;
import org.verapdf.pd.PDObject;
import org.verapdf.pd.colors.PDColorSpace;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class ICCProfile extends PDObject {

	private static final Logger LOGGER = Logger.getLogger(ICCProfile.class.getCanonicalName());

	/** Length of icc profile header */
	public static final int HEADER_LENGTH = 128;
	/** Length of icc profile tag info */
	public static final int TAGINFO_LENGTH = 12;
	/** Offset of cmm type in header */
	public static final int CMM_TYPE_OFFSET = 4;
	/** Offset of device class in header */
	public static final int DEVICE_CLASS_OFFSET = 12;
	/** Offset of color space in header */
	public static final int COLOR_SPACE_OFFSET = 16;
	/** Offset of device manufacturer in header */
	public static final int DEVICE_MANUFACTURER_OFFSET = 48;
	/** Offset of device model in header */
	public static final int DEVICE_MODEL_OFFSET = 52;
	/** Offset of rendering intent in header */
	public static final int RENDERING_INTENT_OFFSET = 64;
	/** Offset of creator in header */
	public static final int CREATOR_OFFSET = 80;
	/** Offset of profile id in header */
	public static final int PROFILE_ID_OFFSET = 84;
	/** Expected length for device class and so on */
	public static final int REQUIRED_LENGTH = 4;
	/** Expected length for profile id */
	public static final int PROFILE_ID_LENGTH = 16;
	/** Expected length version */
	public static final int VERSION_LENGTH = 3;
	/** Offset of version byte */
	public static final int VERSION_BYTE = 8;
	/** Offset of subversion byte */
	public static final int SUBVERSION_BYTE = 9;
	/** Offset of creation year byte */
	private static final int CREATION_YEAR_OFFSET = 24;
	/** Offset of creation month byte */
	private static final int CREATION_MONTH_OFFSET = 26;
	/** Offset of creation day byte */
	private static final int CREATION_DAY_OFFSET = 28;
	/** Offset of creation hour byte */
	private static final int CREATION_HOUR_OFFSET = 30;
	/** Offset of creation min byte */
	private static final int CREATION_MIN_OFFSET = 32;
	/** Offset of creation sec byte */
	private static final int CREATION_SEC_OFFSET = 34;
	/** Offset of profile flags */
	private static final int PROFILE_FLAGS_OFFSET = 44;

	private byte[] profileHeader = new byte[0];
	private byte[] md5ByteValue;
	private Calendar creationDate;
	private boolean isLooksValid = true;
	private String description = null;
	private String copyright = null;

	public ICCProfile(COSObject profileStream) {
		super(profileStream);
		initializeProfileHeader();
	}

	private void initializeProfileHeader() {
		try (ASInputStream data = this.getObject().getData(COSStream.FilterFlags.DECODE)) {
			byte[] temp = new byte[HEADER_LENGTH];
			int count = Math.max(data.read(temp, HEADER_LENGTH), 0);
			if (count == HEADER_LENGTH) {
				this.profileHeader = temp;
			} else {
				this.profileHeader = Arrays.copyOf(temp, count);
			}
			this.creationDate = parseCreationDate(this.profileHeader);
			if (this.profileHeader.length != HEADER_LENGTH) {
				this.isLooksValid = false;
			}
			parseTags(data);
		} catch (IOException e) {
			this.isLooksValid = false;
			LOGGER.log(Level.FINE, "Exception during obtaining ICCProfile header", e);
		}
	}

	public String getMD5() {
		String profileID = getProfileID();
		if (profileID != null) {
			return profileID;
		}
		if (md5ByteValue == null) {
			int iccProfileSize = getSize(profileHeader);
			try (ASInputStream data = this.getObject().getData(COSStream.FilterFlags.DECODE)) {
				byte[] buffer = getICCProfileBytes(data, iccProfileSize);
				if (buffer.length != iccProfileSize) {
					md5ByteValue = new byte[0];
					return null;
				}
				setZero(buffer, PROFILE_FLAGS_OFFSET, PROFILE_FLAGS_OFFSET + REQUIRED_LENGTH);
				setZero(buffer, RENDERING_INTENT_OFFSET, RENDERING_INTENT_OFFSET + REQUIRED_LENGTH);
				setZero(buffer, PROFILE_ID_OFFSET, PROFILE_ID_OFFSET + PROFILE_ID_LENGTH);
				MessageDigest md5  = MessageDigest.getInstance("MD5");
				md5.update(buffer);
				md5ByteValue = md5.digest();
			} catch (NoSuchAlgorithmException | IOException  e) {
				LOGGER.log(Level.FINE, "Exception during calculating ICCProfile md5 value", e);
				md5ByteValue = new byte[0];
				return null;
			}
		}
		if (md5ByteValue != null && isNotAllZero(md5ByteValue)) {
			return new String(md5ByteValue, StandardCharsets.ISO_8859_1);
		}
		return null;
	}

	private static byte[] getICCProfileBytes(ASInputStream data, int iccProfileSize) throws IOException {
		byte[] buffer = new byte[iccProfileSize];
		int bufferSize = 2048;
		byte[] temp = new byte[bufferSize];
		int size = 0;
		int read = data.read(temp, bufferSize);
		while (read != -1) {
			if (size + read >= iccProfileSize) {
				System.arraycopy(temp, 0, buffer, size, iccProfileSize - size);
				return buffer;
			}
			System.arraycopy(temp, 0, buffer, size, read);
			size += read;
			read = data.read(temp, bufferSize);
		}
		return Arrays.copyOf(buffer, size);
	}

	private void setZero(byte[] buffer, int offset, int end) {
		if (end > buffer.length) {
			return;
		}
		for (int i = offset;  i < end; i++) {
			buffer[i] = 0;
		}
	}

	/**
	 * @return true if all necessary fields can be obtained from icc profile header
	 */
	public boolean isLooksValid() {
		return this.isLooksValid;
	}

	/**
	 * @return string representation of device class or null, if profile length
	 *         is too small
	 */
	public String getDeviceClass() {
		return getSubArrayFromHeader(DEVICE_CLASS_OFFSET, REQUIRED_LENGTH);
	}

	/**
	 * @return string representation of color space or null, if profile length
	 *         is too small
	 */
	public String getColorSpace() {
		return getSubArrayFromHeader(COLOR_SPACE_OFFSET, REQUIRED_LENGTH);
	}

	/**
	 * @return string representation of cmm type or null, if profile length
	 *         is too small
	 */
	public String getCMMType() {
		return getSubArrayFromHeader(CMM_TYPE_OFFSET, REQUIRED_LENGTH);
	}

	/**
	 * @return string representation of creator or null, if profile length
	 *         is too small
	 */
	public String getCreator() {
		return getSubArrayFromHeader(CREATOR_OFFSET, REQUIRED_LENGTH);
	}

	/**
	 * @return calendar representation of creation date or null, if profile length
	 *         is too small or contains zero value of creation date
	 */
	public Calendar getCreationDate() {
		return this.creationDate;
	}

	/**
	 * @return string representation of rendering intent or null, if profile length
	 *         is too small
	 */
	public String getRenderingIntent() {
		if (RENDERING_INTENT_OFFSET + REQUIRED_LENGTH > this.profileHeader.length) {
			return null;
		}
		String str = getSubArrayFromHeader(RENDERING_INTENT_OFFSET, REQUIRED_LENGTH);
		if (str == null) {
			return "Perceptual";
		}
		switch (str) {
			case "\u0000\u0000\u0000\u0001":
				return "Media-Relative Colorimetric";
			case "\u0000\u0000\u0000\u0002":
				return "Saturation";
			case "\u0000\u0000\u0000\u0003":
				return "ICC-Absolute Colorimetric";
			default:
				return str;
		}
	}

	/**
	 * @return string representation of profile id or null, if profile length
	 *         is too small
	 */
	public String getProfileID() {
		return getSubArrayFromHeader(PROFILE_ID_OFFSET, PROFILE_ID_LENGTH);
	}

	/**
	 * @return string representation of device model or null, if profile length
	 *         is too small
	 */
	public String getDeviceModel() {
		return getSubArrayFromHeader(DEVICE_MODEL_OFFSET, REQUIRED_LENGTH);
	}

	/**
	 * @return string representation of device manufacturer or null, if profile length
	 *         is too small
	 */
	public String getDeviceManufacturer() {
		return getSubArrayFromHeader(DEVICE_MANUFACTURER_OFFSET, REQUIRED_LENGTH);
	}

	private String getSubArrayFromHeader(int start, int length) {
		return getSubArray(this.profileHeader, start, length);
	}

	private static String getSubArray(byte[] bytes, int start, int length) {
		if (start + length <= bytes.length) {
			byte[] buffer = Arrays.copyOfRange(bytes, start, start + length);
			if (isNotAllZero(buffer)) {
				return new String(buffer, StandardCharsets.ISO_8859_1);
			} else {
				return null;
			}
		}
		LOGGER.log(Level.FINE, "Length of given byte array less than " + (start + length));
		return null;
	}

	private static boolean isNotAllZero(byte[] buffer) {
		for (byte b : buffer) {
			if (b != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return version of ICC profile or null, if profile length is too small
	 */
	public Double getVersion() {
		if (this.profileHeader.length > SUBVERSION_BYTE) {
			StringBuilder version = new StringBuilder(VERSION_LENGTH);
			version.append(this.profileHeader[VERSION_BYTE] & 0xFF).append('.');
			version.append((this.profileHeader[SUBVERSION_BYTE] >>> REQUIRED_LENGTH) & 0xFF);

			return Double.valueOf(version.toString());
		}
		LOGGER.log(Level.FINE, "ICC profile contain less than 10 bytes of data.");
		return null;
	}

	/**
	 * @return copyright of ICC profile or null, if profile length is too small or tag is missing
	 */
	public String getCopyright() {
		return this.copyright;
	}

	/**
	 * @return description of ICC profile or null, if profile length is too small or tag is missing
	 */
	public String getDescription() {
		return this.description;
	}

	private static Calendar parseCreationDate(byte[] header) {
		int year = getCreationPart(header, CREATION_YEAR_OFFSET);
		int month = getCreationPart(header, CREATION_MONTH_OFFSET);
		int day = getCreationPart(header, CREATION_DAY_OFFSET);
		int hour = getCreationPart(header, CREATION_HOUR_OFFSET);
		int min = getCreationPart(header, CREATION_MIN_OFFSET);
		int sec = getCreationPart(header, CREATION_SEC_OFFSET);

		if (year != 0 || month != 0 || day != 0 || hour != 0 || min != 0 || sec != 0) {
			GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
			cal.set(year, month - 1, day, hour, min, sec);
			cal.set(Calendar.MILLISECOND, 0);
			return cal;
		}
		return null;
	}

	private static int getCreationPart(byte[] header, int off) {
		if (header.length < off + 2) {
			return 0;
		}
		int part = header[off] & 0xFF;
		part <<= 8;
		part += header[off + 1] & 0xFF;
		return part;
	}

	private static int getSize(byte[] header) {
		if (header.length < 4) {
			return header.length;
		}
		int part = header[0] & 0xFF;
		for (int i = 1; i < 4; i++) {
			part <<= 8;
			part += header[i] & 0xFF;
		}
		return part;
	}

	private void parseTags(ASInputStream data) throws IOException {
		data.reset();
		int currentOffset = data.skip(HEADER_LENGTH);
		if (currentOffset != HEADER_LENGTH) {
			return;
		}

		byte[] temp = new byte[REQUIRED_LENGTH];
		currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
		if (currentOffset != HEADER_LENGTH + REQUIRED_LENGTH) {
				return;
		}
		int tagsNumberRemained = byteArrayToInt(temp);
		int descOffset = 0;
		int descLength = 0;
		int cprtOffset = 0;
		int cprtLength = 0;
		while (tagsNumberRemained-- > 0) {
			int prevOffset = currentOffset;
			currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
			String tag = new String(temp, StandardCharsets.ISO_8859_1);
			if ("desc".equals(tag)) {
				currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
				descOffset = byteArrayToInt(temp);
				currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
				descLength = byteArrayToInt(temp);
			} else if ("cprt".equals(tag)) {
				currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
				cprtOffset = byteArrayToInt(temp);
				currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
				cprtLength = byteArrayToInt(temp);
			} else {
				currentOffset += data.skip(TAGINFO_LENGTH - REQUIRED_LENGTH);
			}

			if (currentOffset != prevOffset + TAGINFO_LENGTH) {
				return;
			}
		}
		if (descLength != 0) {
			this.description = getTagValue(data, descOffset, descLength, false);
		}
		if (cprtLength != 0) {
			this.copyright = getTagValue(data, cprtOffset, cprtLength, true);
		}
	}

	private static String getTagValue(ASInputStream data, int tagOffset, int tagLength, boolean isCprt) throws IOException {
		data.reset();
		int currOffset = data.skip(tagOffset);
		if (currOffset != tagOffset) {
			return null;
		}

		byte[] temp = new byte[REQUIRED_LENGTH];
		currOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
		if (currOffset != tagOffset + REQUIRED_LENGTH) {
			return null;
		}
		String type = new String(temp, StandardCharsets.ISO_8859_1);
		if ("mluc".equals(type)) {
			int prevOffset = currOffset;

			currOffset += data.skip(REQUIRED_LENGTH);
			currOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
			currOffset += data.skip(REQUIRED_LENGTH);
			if (currOffset != prevOffset + REQUIRED_LENGTH*3) {
				return null;
			}
			int number = byteArrayToInt(temp);
			for (int i = 0; i < number; ++i) {
				prevOffset = currOffset;

				currOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
				String local = getSubArray(temp, 0, REQUIRED_LENGTH);
				if ("enUS".equals(local)) {
					currOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
					int length = byteArrayToInt(temp);
					currOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
					int offset = byteArrayToInt(temp);
					if (currOffset != prevOffset + REQUIRED_LENGTH*3) {
						return null;
					}
					data.reset();
					currOffset = data.skip(offset);
					byte[] temporary = new byte[length];
					currOffset += Math.max(data.read(temporary, length), 0);
					if (currOffset == offset + length) {
						return new String(temporary, StandardCharsets.UTF_16BE).trim();
					}
					return null;
				}
				currOffset += data.skip(REQUIRED_LENGTH*2);
				if (currOffset != prevOffset + REQUIRED_LENGTH*3) {
					return null;
				}
			}
		} else if ("desc".equals(type)) {
			int prevOffset = currOffset;
			currOffset += data.skip(REQUIRED_LENGTH);
			currOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
			if (currOffset != prevOffset + REQUIRED_LENGTH*2) {
				return null;
			}
			int length = byteArrayToInt(temp);
			byte[] temporary = new byte[length];
			currOffset += Math.max(data.read(temporary, length), 0);
			if (currOffset == prevOffset + REQUIRED_LENGTH*2 + length) {
				return new String(temporary, StandardCharsets.US_ASCII).trim();
			}
		} else if (isCprt) {
			int prevOffset = currOffset;
			int length = tagLength - REQUIRED_LENGTH;
			byte[] temporary = new byte[length];
			currOffset += Math.max(data.read(temporary, length), 0);
			if (currOffset == prevOffset + length) {
				return new String(temporary, StandardCharsets.US_ASCII).trim();
			}
		}
		return null;
	}

	private static int byteArrayToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < 4; ++i) {
			int shift = (3 - i) * 8;
			value += (b[i] & 0xFF) << shift;
		}
		return value;
	}

	/**
	 * @return number of colorants for ICC profile, described in profile
	 *         dictionary
	 */
	public Long getNumberOfColorants() {
		return getObject().getIntegerKey(ASAtom.N);
	}

	/**
	 * @return range array value for ICC profile, described in profile
	 *         dictionary
	 */
	public double[] getRange() {
		COSObject rangeObject = getObject().getKey(ASAtom.RANGE);
		if (rangeObject != null && rangeObject.getType() == COSObjType.COS_ARRAY) {
			int size = rangeObject.size();
			Long estimatedSize = getNumberOfColorants();
			if (estimatedSize != null && size != estimatedSize.intValue()*2) {
				LOGGER.log(Level.FINE, "Range array doesn't consist of " + estimatedSize.intValue()*2 + " elements");
			}

			double[] res = new double[size];
			for (int i = 0; i < size; ++i) {
				COSObject number = rangeObject.at(i);
				if (number == null || number.getReal() == null) {
					LOGGER.log(Level.FINE, "Range array contains non number value");
					return null;
				}
				res[i] = number.getReal();
			}
			return res;
		}
		return null;
	}

	public PDColorSpace getAlternate() {
		return ColorSpaceFactory.getColorSpace(getKey(ASAtom.ALTERNATE));
	}

	public PDMetadata getMetadata() {
		COSObject metadata = getKey(ASAtom.METADATA);
		if (metadata != null && metadata.getType() == COSObjType.COS_STREAM) {
			return new PDMetadata(metadata);
		}
		return null;
	}
}
