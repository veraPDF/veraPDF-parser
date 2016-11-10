package org.verapdf.external;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDMetadata;
import org.verapdf.pd.PDObject;
import org.verapdf.pd.colors.PDColorSpace;

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


	private byte[] profileHeader = new byte[0];
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
		String str = getSubArrayFromHeader(RENDERING_INTENT_OFFSET, REQUIRED_LENGTH);
		if (str == null) {
			return null;
		}
		switch (str) {
			case "\u0000\u0000\u0000\u0000":
				return "Perceptual";
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
			return new String(buffer);
		}
		LOGGER.log(Level.FINE, "Length of given byte array less than " + (start + length));
		return null;
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

	private void parseTags(ASInputStream data) throws IOException {
		data.reset();
		int currentOffset = data.skip(HEADER_LENGTH);
		if (currentOffset != HEADER_LENGTH) {
			return;
		}

		int descOffset = 0;
		int descLength = 0;
		int cprtOffset = 0;
		int cprtLength = 0;

		byte[] temp = new byte[REQUIRED_LENGTH];
		currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
		if (currentOffset != HEADER_LENGTH + REQUIRED_LENGTH) {
				return;
		}
		int tagsNumberRemained = byteArrayToInt(temp);
		while (tagsNumberRemained-- > 0) {
			int prevOffset = currentOffset;
			currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
			String tag = new String(temp);
			if (tag.equals("desc")) {
				currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
				descOffset = byteArrayToInt(temp);
				currentOffset += Math.max(data.read(temp, REQUIRED_LENGTH), 0);
				descLength = byteArrayToInt(temp);
			} else if (tag.equals("cprt")) {
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
		String type = new String(temp);
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
			int size = rangeObject.size().intValue();
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
				res[i] = number.getReal().doubleValue();
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
