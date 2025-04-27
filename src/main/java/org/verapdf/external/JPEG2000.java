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

import org.verapdf.as.io.ASInputStream;
import org.verapdf.pd.colors.PDColorSpace;
import org.verapdf.pd.colors.PDDeviceCMYK;
import org.verapdf.pd.colors.PDICCBased;
import org.verapdf.pd.colors.PDLab;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class JPEG2000 {

    private static final Logger LOGGER = Logger.getLogger(JPEG2000.class.getCanonicalName());

    private static final Long DEFAULT_NR_COLOR_CHANNELS = 0L;
    private static final Long DEFAULT_NR_COLOR_SPACE_SPECS = 0L;
    private static final Long DEFAULT_NR_COLOR_SPACES_WITH_APPROX_FIELD = 0L;
    private static final Long DEFAULT_COLR_METHOD = 0L;
    private static final Long DEFAULT_COLR_ENUM_CS = null;
    private static final Long DEFAULT_BIT_DEPTH = 0L;
    private static final Boolean DEFAULT_BPCC_BOX_PRESENT = Boolean.FALSE;
    private static final PDColorSpace DEFAULT_COLOR_SPACE = null;
    private static final double[] ILLUMINANT_D50 = {0.9642, 1.0000, 0.8251};
    private static final byte[] sign = {0x00, 0x00, 0x00, 0x0C, 0x6A, 0x50, 0x20, 0x20, 0x0D, 0x0A, -0x79, 0x0A};

    private static final byte[] header = {0x6A, 0x70, 0x32, 0x68};
    private static final byte[] ihdr = {0x69, 0x68, 0x64, 0x72};
    private static final byte[] bpcc = {0x62, 0x70, 0x63, 0x63};
    private static final byte[] colr = {0x63, 0x6F, 0x6C, 0x72};

    private final Long nrColorChannels;
    private final Long nrColorSpaceSpecs;
    private final Long nrColorSpacesWithApproxField;
    private final Long colrMethod;
    private final Long colrEnumCS;
    private final Long bitDepth;
    private final Boolean bpccBoxPresent;
    private final PDColorSpace colorSpace;

    private JPEG2000(Long nrColorChannels, Long nrColorSpaceSpecs, Long nrColorSpacesWithApproxField, Long colrMethod, Long colrEnumCS, Long bitDepth, Boolean bpccBoxPresent, PDColorSpace colorSpace) {
        this.nrColorChannels = nrColorChannels;
        this.nrColorSpaceSpecs = nrColorSpaceSpecs;
        this.nrColorSpacesWithApproxField = nrColorSpacesWithApproxField;
        this.colrMethod = colrMethod;
        this.colrEnumCS = colrEnumCS;
        this.bitDepth = bitDepth;
        this.bpccBoxPresent = bpccBoxPresent;
        this.colorSpace = colorSpace;
    }

    /**
     * Creates new JPEG2000 object
     *
     * @param stream image stream to parse
     * @return created JPEG2000 object
     */
    public static JPEG2000 fromStream(ASInputStream stream) {
        Builder builder = new Builder();

        byte[] tempSign = new byte[12];
        try {
            // Check if the stream starts with valid jp2 signature
            if (stream.read(tempSign, tempSign.length) != 12 || !isValidSignature(tempSign)) {
                LOGGER.log(Level.FINE, "File contains wrong signature");
                return builder.build();
            }
            // Finding the beginning of the header box content
            long headerLeft = findHeader(stream);

            if (headerLeft >= 0) {
                parseHeader(stream, headerLeft, builder);
            }

        } catch (IOException e) {
            LOGGER.log(Level.FINE, "IO Exception reading JP2K stream", e);
        }
        return builder.build();
    }

    private static void parseHeader(final ASInputStream stream, final long headerLeft, final Builder builder) throws IOException {
        long leftInHeader = headerLeft;
        boolean isHeaderReachEnd = leftInHeader == 0;
        Long nrColorSpaceSpecs = null;
        Long nrColorSpacesWithApproxField = null;
        Long firstColrMethod = null;
        Long firstColrEnumCS = null;
        Long colrMethod = null;
        Long colrEnumCS = null;
        Boolean doesFirstContainsColorSpace = null;
        PDColorSpace firstColorSpace = null;
        PDColorSpace colorSpace = null;

        while (true) {
            byte[] lbox = new byte[4];
            byte[] tbox = new byte[4];
            if (stream.read(lbox, lbox.length) != 4 || stream.read(tbox, tbox.length) != 4) {
                break;
            }
            int skipped = 8;
            long length = convertArrayToLong(lbox);
            if (length == 1) {
                byte[] xlbox = new byte[8];
                if (stream.read(xlbox, xlbox.length) != 8) {
                    break;
                }
                length = convertArrayToLong(xlbox);
                skipped = 16;
            }
            if (length < 0 || (!isHeaderReachEnd && (length == 0 || length > leftInHeader))) {
                break;
            }
            long leftInBox = length - skipped;

            if (matches(tbox, ihdr)) {
                if (leftInBox != 14 && length != 0) {
                    LOGGER.log(Level.FINE, "Image header content does not contain 14 bytes");
                    break;
                }
                skipBytes(stream, 8);
                byte[] nc = new byte[2];
                if (stream.read(nc, nc.length) != 2) {
                    LOGGER.log(Level.FINE, "Can not read number of components");
                    break;
                }
                long ncColorChannels = convertArrayToLong(nc);
                builder.nrColorChannels(ncColorChannels);
                byte[] bpc = new byte[1];
                if (stream.read(bpc, bpc.length) != 1) {
                    LOGGER.log(Level.FINE, "Can not read bitDepth");
                    break;
                }
                long bitDepth = bpc[0] + 1;
                builder.bitDepth(bitDepth);
                skipBytes(stream, 3);
            } else if (matches(tbox, bpcc)) {
                builder.bpccBoxPresent(Boolean.TRUE);
                skipBytes(stream, leftInBox);
            } else if (matches(tbox, colr)) {
                if (leftInBox < 3) {
                    LOGGER.log(Level.FINE, "Founded 'colr' box with length less than 3");
                    break;
                }
                if (nrColorSpaceSpecs == null) {
                    nrColorSpaceSpecs = 1L;
                } else {
                    ++nrColorSpaceSpecs;
                }
                byte[] meth = new byte[1];
                if (stream.read(meth, meth.length) != 1) {
                    LOGGER.log(Level.FINE, "Can not read METH");
                    break;
                }
                long methValue = convertArrayToLong(meth);
                if (firstColrMethod == null) {
                    firstColrMethod = methValue;
                }
                skipBytes(stream, 1);
                byte[] approx = new byte[1];
                if (stream.read(approx, approx.length) != 1) {
                    LOGGER.log(Level.FINE, "Can not read APPROX");
                    break;
                }
                long approxValue = convertArrayToLong(approx);
                if (approxValue == 1) {
                    if (nrColorSpacesWithApproxField == null) {
                        nrColorSpacesWithApproxField = 1L;
                    } else {
                        ++nrColorSpacesWithApproxField;
                    }
                    if (colrMethod == null) {
                        colrMethod = methValue;
                    }
                }
                long read = 3;
                if (methValue == 1) {
                    if (leftInBox < 7) {
                        LOGGER.log(Level.FINE, "Founded 'colr' box with meth value 1 and length less than 7");
                        break;
                    }
                    byte[] enumCS = new byte[4];
                    if (stream.read(enumCS, enumCS.length) != 4) {
                        LOGGER.log(Level.FINE, "Can not read EnumCS");
                        break;
                    }
                    read += 4;
                    long enumCSValue = convertArrayToLong(enumCS);
                    if (firstColrEnumCS == null) {
                        firstColrEnumCS = enumCSValue;
                        firstColorSpace = createColorSpaceFromEnumValue(firstColrEnumCS);
                        doesFirstContainsColorSpace = firstColorSpace != null;
                    }
                    if (approxValue == 1 && colrEnumCS == null) {
                        colrEnumCS = enumCSValue;
                        colorSpace = createColorSpaceFromEnumValue(colrEnumCS);
                    }
                } else if (methValue == 2) {
                    int profileLength = (int) (leftInBox - read);
                    byte[] profile = new byte[profileLength];
                    if (stream.read(profile, profileLength) != profileLength) {
                        LOGGER.log(Level.FINE, "Can not read Profile");
                        break;
                    }
                    read += profileLength;
                    if (doesFirstContainsColorSpace == null) {
                        firstColorSpace = createColorSpaceFromProfile(profile);
                        doesFirstContainsColorSpace = firstColorSpace != null;
                    }
                    if (approxValue == 1 && colorSpace == null) {
                        colorSpace = createColorSpaceFromProfile(profile);
                    }
                }
                skipBytes(stream, leftInBox - read);
            } else {
                skipBytes(stream, leftInBox);
            }

            leftInHeader -= length;
            if ((isHeaderReachEnd && length == 0) || (!isHeaderReachEnd && leftInHeader == 0)) {
                break;
            }
        }

        if (nrColorSpaceSpecs != null) {
            builder.nrColorSpaceSpecs(nrColorSpaceSpecs);
        }
        if (nrColorSpacesWithApproxField != null) {
            builder.nrColorSpacesWithApproxField(nrColorSpacesWithApproxField);
        }

        if (nrColorSpacesWithApproxField != null) {
            if (colrMethod != null) {
                builder.colrMethod(colrMethod);
            }
            if (colrEnumCS != null) {
                builder.colrEnumCS(colrEnumCS);
            }
            if (colorSpace != null) {
                builder.colorSpace(colorSpace);
            }
        } else if (Long.valueOf(1L).equals(nrColorSpaceSpecs)) {
            if (firstColrMethod != null) {
                builder.colrMethod(firstColrMethod);
            }
            if (firstColrEnumCS != null) {
                builder.colrEnumCS(firstColrEnumCS);
            }
            if (firstColorSpace != null) {
                builder.colorSpace(firstColorSpace);
            }
        }
    }

    private static PDColorSpace createColorSpaceFromEnumValue(long enumCS) {
        if (enumCS > Integer.MAX_VALUE) {
            return null;
        }

        switch ((int) enumCS) {
            case 12:
                return PDDeviceCMYK.INSTANCE;
            case 14:
                return new PDLab(ILLUMINANT_D50);
                //todo parsing EP field
            case 17:
                return new PDICCBased(1);
            case 16:
            case 18:
            case 20:
            case 21:
            case 24:
                return new PDICCBased(3);
            default:
                return null;
        }
    }

    private static PDICCBased createColorSpaceFromProfile(byte[] profile) {
        if (profile.length < 20) {
            return null;
        }

        String type = new String(profile, 16, 4, StandardCharsets.ISO_8859_1);
        int nrOfComp;
        switch (type) {
            case "GRAY":
                nrOfComp = 1;
                break;
            case "2CLR":
                nrOfComp = 2;
                break;
            case "XYZ ":
            case "Lab ":
            case "Luv ":
            case "YCbr":
            case "Yxy ":
            case "RGB ":
            case "HSV ":
            case "HLS ":
            case "CMY ":
            case "3CLR":
                nrOfComp = 3;
                break;
            case "CMYK":
            case "4CLR":
                nrOfComp = 4;
                break;
            case "5CLR":
                nrOfComp = 5;
                break;
            case "6CLR":
                nrOfComp = 6;
                break;
            case "7CLR":
                nrOfComp = 7;
                break;
            case "8CLR":
                nrOfComp = 8;
                break;
            case "9CLR":
                nrOfComp = 9;
                break;
            case "ACLR":
                nrOfComp = 10;
                break;
            case "BCLR":
                nrOfComp = 11;
                break;
            case "CCLR":
                nrOfComp = 12;
                break;
            case "DCLR":
                nrOfComp = 13;
                break;
            case "ECLR":
                nrOfComp = 14;
                break;
            case "FCLR":
                nrOfComp = 15;
                break;
            default:
                LOGGER.log(Level.FINE, "Unknown color space signature in ICC Profile of image. Current signature: " + type);
                return null;
        }
        return new PDICCBased(nrOfComp, profile);
    }

    /**
     * Finds the beginning of the header box content and returns its left length
     *
     * @param stream image stream
     * @return left length of the header box or -1 if it has not been found and 0 if it ends at the end of the stream
     * @throws IOException
     */
    private static long findHeader(ASInputStream stream) throws IOException {
        while (true) {
            byte[] lbox = new byte[4];
            byte[] tbox = new byte[4];
            if (stream.read(lbox, lbox.length) != 4 || stream.read(tbox, tbox.length) != 4) {
                return -1L;
            }
            int skipped = 8;
            long length = convertArrayToLong(lbox);
            if (length == 1) {
                byte[] xlbox = new byte[8];
                if (stream.read(xlbox, xlbox.length) != 8) {
                    return -1L;
                }
                length = convertArrayToLong(xlbox);
                skipped = 16;
            }
            long left = length - skipped;
            // Check is current box a header
            if (matches(tbox, header)) {
                if (length == 0) {
                    return 0;
                }
				return left <= 0 ? -1L : left;
            }
			if (length == 0 || left < 0) {
			    return -1L;
			}
			skipBytes(stream, left);
        }
    }

    private static void skipBytes(ASInputStream stream, long skipNumber) throws IOException {
        while (skipNumber > 0) {
            int needToSkip = (int) Math.min(skipNumber, Integer.MAX_VALUE);
            int skipped = stream.skip(needToSkip);
            if (skipped == 0) {
                break;
            }
            skipNumber -= skipped;
        }
    }

    private static long convertArrayToLong(byte[] toConvert) {
        if (toConvert.length < 1 || toConvert.length > 8) {
            throw new IllegalArgumentException("Length of the converting byte array can not be greater than 8");
        }
        long res = 0;
        for (byte aToConvert : toConvert) {
            res <<= 8;
            res += aToConvert & 0xff;
        }
        return res;
    }

    private static boolean isValidSignature(byte[] signature) {
        return matches(signature, sign);
    }

    private static boolean matches(byte[] source, byte[] match) {
        if (match.length != source.length) {
            return false;
        }
        for (int i = 0; i < match.length; ++i) {
            if (source[i] != match[i]) {
                return false;
            }
        }
        return true;
    }

    public PDColorSpace getImageColorSpace() {
        return this.colorSpace;
    }

    public Long getNumberOfColorChannels() {
        return this.nrColorChannels;
    }

    public Long getNumberOfColorSpaceSpecs() {
        return this.nrColorSpaceSpecs;
    }

    public Long getNumberOfColorSpacesWithApproxField() {
        return this.nrColorSpacesWithApproxField;
    }

    public Long getColrMethod() {
        return this.colrMethod;
    }

    public Long getColrEnumCS() {
        return this.colrEnumCS;
    }

    public Long getBitDepth() {
        return this.bitDepth;
    }

    public Boolean getBPCCBoxPresent() {
        return this.bpccBoxPresent;
    }

    private static class Builder {
        private Long nrColorChannels = DEFAULT_NR_COLOR_CHANNELS;
        private Long nrColorSpaceSpecs = DEFAULT_NR_COLOR_SPACE_SPECS;
        private Long nrColorSpacesWithApproxField = DEFAULT_NR_COLOR_SPACES_WITH_APPROX_FIELD;
        private Long colrMethod = DEFAULT_COLR_METHOD;
        private Long colrEnumCS = DEFAULT_COLR_ENUM_CS;
        private Long bitDepth = DEFAULT_BIT_DEPTH;
        private Boolean bpccBoxPresent = DEFAULT_BPCC_BOX_PRESENT;
        private PDColorSpace colorSpace = DEFAULT_COLOR_SPACE;

        public Builder() {
			// TODO Auto-generated constructor stub
		}

		public JPEG2000 build() {
            return new JPEG2000(this.nrColorChannels, this.nrColorSpaceSpecs, this.nrColorSpacesWithApproxField, this.colrMethod, this.colrEnumCS, this.bitDepth, this.bpccBoxPresent, this.colorSpace);
        }

        public Builder nrColorChannels(Long nrColorChannels) {
            this.nrColorChannels = nrColorChannels;
            return this;
        }

        public Builder nrColorSpaceSpecs(Long nrColorSpaceSpecs) {
            this.nrColorSpaceSpecs = nrColorSpaceSpecs;
            return this;
        }

        public Builder nrColorSpacesWithApproxField(Long nrColorSpacesWithApproxField) {
            this.nrColorSpacesWithApproxField = nrColorSpacesWithApproxField;
            return this;
        }

        public Builder colrMethod(Long colrMethod) {
            this.colrMethod = colrMethod;
            return this;
        }

        public Builder colrEnumCS(Long colrEnumCS) {
            this.colrEnumCS = colrEnumCS;
            return this;
        }

        public Builder bitDepth(Long bitDepth) {
            this.bitDepth = bitDepth;
            return this;
        }

        public Builder bpccBoxPresent(Boolean bpccBoxPresent) {
            this.bpccBoxPresent = bpccBoxPresent;
            return this;
        }

        public Builder colorSpace(PDColorSpace colorSpace) {
            this.colorSpace = colorSpace;
            return this;
        }

    }
}
