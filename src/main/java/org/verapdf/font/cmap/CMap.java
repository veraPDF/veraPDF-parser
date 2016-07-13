package org.verapdf.font.cmap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents cmap.
 *
 * @author Sergey Shemyakov
 */
public class CMap {
    private int wMode;
    private String registry, ordering;
    private String name;

    private List<CIDMappable> cidMappings;
    private List<CodeSpace> codeSpaces;
    private List<CIDMappable> notDefMappings;

    public CMap() {
        this.cidMappings = new LinkedList<>();
        this.codeSpaces = new ArrayList<>();
        this.notDefMappings = new ArrayList<>();
        wMode = 0; // default
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
        return 0;  //TODO: probably change that to something else.
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
     * Setter for name of CMap.
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Setter for codespace ranges.
     */
    public void setCodeSpaces(List<CodeSpace> codeSpaces) {
        this.codeSpaces = codeSpaces;
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
}
