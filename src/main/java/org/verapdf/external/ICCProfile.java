package org.verapdf.external;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.pd.PDObject;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Maksim Bezrukov
 */
public class ICCProfile extends PDObject {

	/** Length of icc output profile header */
	public static final int HEADER_LENGTH = 128;
	/** Offset of device class in header */
	public static final int DEVICE_CLASS_OFFSET = 12;
	/** Offset of color space in header */
	public static final int COLOR_SPACE_OFFSET = 16;
	/** Expected length for device class and so on */
	public static final int REQUIRED_LENGTH = 4;
	/** Expected length version */
	public static final int VERSION_LENGTH = 3;
	/** Offset of version byte */
	public static final int VERSION_BYTE = 8;
	/** Offset of subversion byte */
	public static final int SUBVERSION_BYTE = 9;

	private byte[] profileHeader;

	private ICCProfile(COSObject profileStream) {
		super(profileStream);
		initializeProfileHeader();
	}

	private void initializeProfileHeader() {
		try (ASInputStream data = this.getObject().getData(COSStream.FilterFlags.DECODE)) {
			byte[] temp = new byte[HEADER_LENGTH];
			int count = data.read(temp, HEADER_LENGTH);
			if (count == HEADER_LENGTH) {
				this.profileHeader = temp;
			} else {
				this.profileHeader = Arrays.copyOf(temp, count);
			}
		} catch (IOException e) {
			// TODO: clear it
			System.out.println("Exception during obtaining ICCProfile header");
			e.printStackTrace();
		}
	}

	/**
	 * @return string representation of device class or null, if profile length
	 *         is too small
	 */
	public String getDeviceClass() {
		return getSubArray(DEVICE_CLASS_OFFSET, REQUIRED_LENGTH);
	}

	/**
	 * @return number of colorants for ICC profile, described in profile
	 *         dictionary
	 */
	public Long getNumberOfColorants() {
		COSObject key = this.getKey(ASAtom.N);
		return key == null ? null : key.getInteger();
	}

	/**
	 * @return string representation of color space or null, if profile length
	 *         is too small
	 */
	public String getColorSpace() {
		return getSubArray(COLOR_SPACE_OFFSET, REQUIRED_LENGTH);
	}

	private String getSubArray(int start, int length) {
		if (start + length <= this.profileHeader.length) {
			byte[] buffer = Arrays.copyOfRange(this.profileHeader, start, start + length);
			return new String(buffer);
		}
		// TODO: replace
		System.out.println("Length of icc profile less than " + (start + length));
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
		// TODO: replace
		System.out.println("ICC profile contain less than 10 bytes of data.");
		return null;
	}
}
