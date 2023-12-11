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
package org.verapdf.pd.font.cff;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class starts parsing for all inner CFF fonts and contains fonts parsed.
 *
 * @author Sergey Shemyakov
 */
public class CFFFontProgram extends CFFFileBaseParser implements FontProgram {

    private static final Logger LOGGER = Logger.getLogger(CFFFontProgram.class.getCanonicalName());
    private FontProgram font;
    private final CMap externalCMap;
    private boolean isCIDFont = false;
    private boolean isFontParsed = false;
    private final boolean isSubset;

    /**
     * Constructor from stream.
     *
     * @param stream is stream with CFF program.
     * @throws IOException if creation of @{link SeekableStream} fails.
     */
    public CFFFontProgram(ASInputStream stream, CMap cMap,
                          boolean isSubset) throws IOException {
        super(stream);
        this.externalCMap = cMap;
        this.isSubset = isSubset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseFont() throws IOException {
        if (!isFontParsed) {
            isFontParsed = true;
            this.readHeader();
            this.readIndex();   // name
            long topOffset = this.source.getOffset();
            CFFIndex top = this.readIndex();
            if (top.size() == 0) {
                LOGGER.log(Level.WARNING, "Error in cff font program parsing: top DICT INDEX is empty.");
                throw new IOException("Error in cff font program parsing: top DICT INDEX is empty.");
            }
            this.definedNames = this.readIndex();
            CFFIndex globalSubrs = this.readIndex();
            if (isCIDFont(top.get(0))) {
                font = new CFFCIDFontProgram(this.source, this.definedNames, globalSubrs,
                        topOffset + top.getOffset(0) - 1 + top.getOffsetShift(),
                        topOffset + top.getOffset(1) - 1 + top.getOffsetShift(),
                        this.externalCMap, this.isSubset);
            } else {
                font = new CFFType1FontProgram(this.source, this.definedNames, globalSubrs,
                        topOffset + top.getOffset(0) - 1 + top.getOffsetShift(),
                        topOffset + top.getOffset(1) - 1 + top.getOffsetShift(),
                        this.externalCMap, this.isSubset);
            }
            font.parseFont();
        }
    }

    private boolean isCIDFont(byte[] topDict) {
        try {
            byte rosOffset;
            int supplementFirstByte = topDict[4] & 0xFF;    // checking if first operator is really ROS
            if (supplementFirstByte < 247 && supplementFirstByte > 31) {
                rosOffset = 5;
            } else if (supplementFirstByte > 246 && supplementFirstByte < 255) {
                rosOffset = 6;
            } else if (supplementFirstByte == 28) {
                rosOffset = 7;
            } else if (supplementFirstByte == 29) {
                rosOffset = 9;
            } else {
                return isContainsROS(topDict);
            }
            if (topDict[rosOffset] == 12 && topDict[rosOffset + 1] == 30) {
                isCIDFont = true;
                return true;
            }
            return isContainsROS(topDict);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return isContainsROS(topDict);
        }
    }

    private boolean isContainsROS(byte[] topDict) {
        for (int rosOffset = 0; rosOffset < topDict.length - 2; rosOffset++) {
            if (topDict[rosOffset] == 12 && topDict[rosOffset + 1] == 30) {
                LOGGER.log(Level.WARNING, "The Top DICT does not begin with ROS operator");
                isCIDFont = true;
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int code) {
        return this.font.getWidth(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String glyphName) {
        return this.font.getWidth(glyphName);
    }

    @Override
    public String getGlyphName(int code) {
        return this.font != null ? this.font.getGlyphName(code) : null;
    }

    /**
     * @return true if this font is CFF CID font.
     */
    public boolean isCIDFont() {
        return isCIDFont;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCode(int code) {
        return font.containsCode(code);
    }

    @Override
    public boolean containsGlyph(String glyphName) {
        return font.containsGlyph(glyphName);
    }

    @Override
    public boolean containsCID(int cid) {
        return font.containsCID(cid);
    }

    @Override
    public boolean isAttemptedParsing() {
        return font != null && font.isAttemptedParsing();
    }

    @Override
    public boolean isSuccessfulParsing() {
        return font != null && font.isSuccessfulParsing();
    }

    @Override
    public List<Integer> getCIDList() {
        if (this.font instanceof CFFCIDFontProgram) {
            return this.font.getCIDList();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @return CID font or Type1 font that is presented by CFF program.
     */
    public FontProgram getFont() {
        return this.font;
    }

    @Override
    public ASFileStreamCloser getFontProgramResource() {
        if (this.source instanceof ASMemoryInStream) {
            return null;
        } else {
            return new ASFileStreamCloser(this.source);
        }
    }

    @Override
    public String getWeight() {
        try {
            this.parseFont();
        } catch (IOException exception) {
            return null;
        }
        return font.getWeight();
    }

    @Override
    public Double getAscent() {
        return null;
    }

    @Override
    public Double getDescent() {
        return null;
    }
}
