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
package org.verapdf.pd.font.cmap;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents cmap.
 *
 * @author Sergey Shemyakov
 */
public class CMap {

    private static final Logger LOGGER = Logger.getLogger(CMap.class.getCanonicalName());

    private int wMode;
    private String registry, ordering;
    private int supplement;
    private String name;
    int shortestCodeSpaceLength;

    private List<CIDMappable> cidMappings;
    private List<CodeSpace> codeSpaces;
    private List<CIDMappable> notDefMappings;
    private Map<Integer, String> toUnicode;
    private List<ToUnicodeInterval> unicodeIntervals;

    public CMap() {
        this.cidMappings = new LinkedList<>();
        this.codeSpaces = new ArrayList<>();
        this.notDefMappings = new ArrayList<>();
        this.toUnicode = new HashMap<>();
        this.unicodeIntervals = new ArrayList<>();
        wMode = 0; // default
        shortestCodeSpaceLength = Integer.MAX_VALUE;
    }

    /**
     * Gets CID for given character.
     *
     * @param character is code of character, for which CID is calculated.
     * @return CID for given character or 0 if it cannot be obtained.
     */
    public int toCID(int character) {
        for (CIDMappable cidMapping : cidMappings) {
            int res = cidMapping.getCID(character);
            if (res != -1) {
                return res;
            }
        }

        for (CIDMappable notDefMapping : notDefMappings) {
            int res = notDefMapping.getCID(character);
            if (res != -1) {
                return res;
            }
        }
        return 0;
    }

    /**
     * @return true if this CMap can convert given code to CID.
     */
    public boolean containsCode(int character) {
        for (CIDMappable cidMapping : cidMappings) {
            int res = cidMapping.getCID(character);
            if (res != -1) {
                return true;
            }
        }

        for (CIDMappable notDefMapping : notDefMappings) {
            int res = notDefMapping.getCID(character);
            if (res != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads character code from input stream and returnes it's CID. This uses
     * codespace information from CMap. Details are described in PDF32000 in
     * 9.7.6.2 "CMap Mapping".
     *
     * @param stream is stream from which character codes will be read.
     * @return CID of read code.
     */
    public int getCodeFromStream(InputStream stream) throws IOException {
        byte[] charCode = new byte[5];
        byte[] temp = new byte[1];
        int previousShortestMatchingCodeSpaceLength = this.shortestCodeSpaceLength;

        for (int i = 0; i <= 4; ++i) {
            temp[0] = (byte) stream.read();
            System.arraycopy(temp, 0, charCode, i, 1);
            byte[] currentCode = Arrays.copyOf(charCode, i + 1);
            for (CodeSpace codeSpace : codeSpaces) {     // Looking for complete match
                if (codeSpace.contains(currentCode)) {
                    int res = (int) CMapParser.numberFromBytes(currentCode);
                    if (res != -1) {
                        return res;
                    }
					LOGGER.log(Level.FINE, "CMap " + this.name + " has invalid codespace information.");
                }
            }
            int shortestMatchingCodeSpaceLength = Integer.MAX_VALUE;
            for (CodeSpace codeSpace : codeSpaces) { // Looking for partial matches on bytes 0, ..., i
                boolean partialMatch = true;
                for (int j = 0; j <= i; ++j) {
                    if (!codeSpace.isPartialMatch(charCode[j], j)) {
                        partialMatch = false;
                        break;
                    }
                }
                if (partialMatch && shortestMatchingCodeSpaceLength >
                        codeSpace.getLength()) {
                    shortestMatchingCodeSpaceLength = codeSpace.getLength();    // Remembering length of shortest partially matching codespace
                }
            }
            if (shortestMatchingCodeSpaceLength == Integer.MAX_VALUE && !codeSpaces.isEmpty()) {
                byte[] tmp = new byte[previousShortestMatchingCodeSpaceLength - i - 1];
                stream.read(tmp);
                System.arraycopy(tmp, 0, charCode, 0, tmp.length);      // No described partial matching, reading necessary amount of bytes and returning 0
                return 0;
            }
            previousShortestMatchingCodeSpaceLength = shortestMatchingCodeSpaceLength;
        }
        return 0;
    }

    /**
     * Method adds all the data needed to this CMap from another CMap.
     *
     * @param another is another CMap.
     */
    public void useCMap(CMap another) {
        this.cidMappings.addAll(another.cidMappings);
        this.codeSpaces.addAll(another.codeSpaces);
        this.notDefMappings.addAll(another.notDefMappings);
        this.toUnicode.putAll(another.toUnicode);
    }

    /**
     * @return writing mode of given CMap.
     */
    public int getwMode() {
        return wMode;
    }

    /**
     * Setter for writing mode of given CMap.
     */
    void setwMode(int wMode) {
        this.wMode = wMode;
    }

    /**
     * @return Registry value from CIDSystemInfo dictionary.
     */
    public String getRegistry() {
        return registry;
    }

    /**
     * Setter for Registry.
     */
    void setRegistry(String registry) {
        this.registry = registry;
    }

    /**
     * @return Ordering value from CIDSystemInfo dictionary.
     */
    public String getOrdering() {
        return ordering;
    }

    /**
     * Setter for Ordering.
     */
    void setOrdering(String ordering) {
        this.ordering = ordering;
    }

    /**
     * @return supplement of CMap.
     */
    public int getSupplement() {
        return supplement;
    }

    public void setSupplement(int supplement) {
        this.supplement = supplement;
    }

    /**
     * Setter for name of CMap.
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Setter for codespace ranges.
     */
    void setCodeSpaces(List<CodeSpace> codeSpaces) {
        this.codeSpaces = codeSpaces;
    }

    void addUnicodeMapping(int code, String toUnicodeMap) {
        this.toUnicode.put(Integer.valueOf(code), toUnicodeMap);
    }

    /**
     * Returns Unicode sequence for given character code.
     *
     * @param code is code of character.
     * @return Unicode sequence obtained from this CMap.
     */
    public String getUnicode(int code) {
        String res = this.toUnicode.get(Integer.valueOf(code));
        if(res == null) {
            for(ToUnicodeInterval interval : this.unicodeIntervals) {
                if(interval.containsCode(code)) {
                    return interval.toUnicode(code);
                }
            }
        }
        return res;
    }

    /**
     * @return name of this CMap.
     */
    public String getName() {
        return name;
    }

    void addNotDefInterval(NotDefInterval interval) {
        this.notDefMappings.add(interval);
    }

    List<CodeSpace> getCodeSpaces() {
        return codeSpaces;
    }

    void addCidInterval(CIDInterval interval) {
        this.cidMappings.add(0, interval);
    }

    void addSingleCidMapping(SingleCIDMapping mapping) {
        this.cidMappings.add(0, mapping);
    }

    void addSingleNotDefMapping(SingleCIDMapping mapping) {
        this.notDefMappings.add(mapping);
    }

    void addUnicodeInterval(ToUnicodeInterval interval) {
        this.unicodeIntervals.add(interval);
    }

    public List<CIDMappable> getCidMappings() {
        return cidMappings;
    }
}
