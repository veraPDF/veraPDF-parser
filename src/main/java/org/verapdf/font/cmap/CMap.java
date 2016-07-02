package org.verapdf.font.cmap;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * This class represents cmap.
 *
 * @author Sergey Shemyakov
 */
public class CMap {
    private int wMode;
    private String registry, ordering;
    private String name;

    private static final Logger LOGGER = Logger.getLogger(CMap.class);

    //This contains all CIDs specified in cidchar and notdefchar
    private Map<Integer, Integer> singleCidMapping;

    private List<CIDInterval> cidMappingIntervals;
    private List<CodeSpace> codeSpaces;
    private List<NotDefInterval> notDefIntervals;

    public CMap() {
        this.singleCidMapping = new HashMap<>();
        this.cidMappingIntervals = new LinkedList<>();
        this.codeSpaces = new ArrayList<>();
        this.notDefIntervals = new ArrayList<>();
        wMode = 0; // default
    }

    /**
     * Gets CID for given character.
     *
     * @param character is code of character, for which CID is calculated.
     * @return CID for given character.
     */
    public int toCID(int character) {
        Integer cid = singleCidMapping.get(character);
        if (cid != null) {
            return cid;
        } else {
            for (CIDInterval interval : cidMappingIntervals) {
                if (interval.contains(character)) {
                    return interval.getCID(character);
                }
            }
            return 0;
        }
    }

    void addSingleMapping(int key, int value) {
        if (!singleMappingNotRepeating(key)) {
            LOGGER.warn("CMap " + this.name + " contains overlapping CID information");
            return;
        }
        this.singleCidMapping.put(key, value);
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
    public void setwMode(int wMode) {
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
    public void setRegistry(String registry) {
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
    public void setOrdering(String ordering) {
        this.ordering = ordering;
    }

    /**
     * Setter for name of CMap.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Setter for codespace ranges.
     */
    public void setCodeSpaces(List<CodeSpace> codeSpaces) {
        this.codeSpaces = codeSpaces;
    }

    private boolean singleMappingNotRepeating(int key) {
        if (singleCidMapping.containsKey(key)) {
            return true;
        }
        for (CIDInterval interval : cidMappingIntervals) {
            if (interval.contains(key)) {
                return true;
            }
        }
        for (NotDefInterval interval : notDefIntervals) {
            if (interval.contains(key)) {
                return true;
            }
        }
        return false;
    }
}
