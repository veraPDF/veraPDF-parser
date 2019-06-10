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

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.PDObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents CMap on PD layer.
 *
 * @author Sergey Shemyakov
 */
public class PDCMap extends PDObject {

    private static final Logger LOGGER = Logger.getLogger(PDCMap.class.getCanonicalName());

    private COSDictionary cidSystemInfo;
    private CMap cMapFile = null;
    private PDCMap useCMap = null;
    private boolean parsedCMap = false;
    private Boolean isIdentity;

    /**
     * Constructor from COSObject.
     *
     * @param cMap is COSStream containing CMap or COSName containing name of
     *             predefined CMap.
     */
    public PDCMap(COSObject cMap) {
        super(cMap == null ? COSObject.getEmpty() : cMap);
    }

    /**
     * @return name of this CMap.
     */
    public String getCMapName() {
        if (this.getObject().getType() == COSObjType.COS_NAME) {
            return getObject().getString();
        }
        if (this.getObject().getType() == COSObjType.COS_STREAM) {
            COSObject cMapName = this.getObject().getKey(ASAtom.CMAPNAME);
            if (!cMapName.empty()) {
                return cMapName.getString();
            }
        }
        return "";
    }

    private String getCMapID() {
        if (this.getObject().getType() == COSObjType.COS_STREAM) {
            return "CMap " + getObject().getObjectKey().toString();
        } else if (this.getObject().getType() == COSObjType.COS_NAME) {
            return getObject().getString();
        }
        return "";
    }

    /**
     * @return COSObject, representing this CMap.
     */
    public COSObject getcMap() {
        return getObject();
    }

    /**
     * @return CMap file object read from stream or loaded from predefined CMap
     * file or null if load failed.
     */
    public CMap getCMapFile() {
        if (!parsedCMap) {
            parsedCMap = true;
            if (this.getObject().getType() == COSObjType.COS_STREAM) {
                try (ASInputStream cMapStream = this.getObject().getData(COSStream.FilterFlags.DECODE)) {
                    this.cMapFile = CMapFactory.getCMap(getCMapID(), cMapStream);
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Can't close stream", e);
                }
            } else if (this.getObject().getType() == COSObjType.COS_NAME) {
                String name = this.getObject().getString();
                String cMapPath = "/font/cmap/" + name;
                try (ASInputStream cMapStream = loadCMap(cMapPath)) {
                    this.cMapFile = CMapFactory.getCMap(getCMapID(), cMapStream);
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Can't close stream", e);
                }
            } else {
                return null;
            }
            parseUseCMap();
            if (useCMap != null) {
                this.cMapFile.useCMap(useCMap.getCMapFile());
            }
        }
        return this.cMapFile;
    }

    /**
     * @return Registry value from CMap CIDSystemInfo dictionary.
     */
    public String getRegistry() {
        if (this.getCIDSystemInfo() == null) {
            return null;
        }
        return this.getCIDSystemInfo().getStringKey(ASAtom.REGISTRY);
    }

    /**
     * @return Ordering value from CMap CIDSystemInfo dictionary.
     */
    public String getOrdering() {
        if (this.getCIDSystemInfo() == null) {
            return null;
        }
        return this.getCIDSystemInfo().getStringKey(ASAtom.ORDERING);
    }

    /**
     * @return Supplement value from CMap CIDSystemInfo dictionary.
     */
    public Long getSupplement() {
        if (this.getCIDSystemInfo() == null) {
            return null;
        }
        return this.getCIDSystemInfo().getIntegerKey(ASAtom.SUPPLEMENT);
    }

    /**
     * @return COSObject of the referenced cMap.
     */
    public COSObject getUseCMap() {
        COSObject res = this.getObject().getKey(ASAtom.USE_CMAP);
        return res == null ? COSObject.getEmpty() : res;
    }

    private COSDictionary getCIDSystemInfo() {
        if (this.getObject().getType() == COSObjType.COS_NAME) {
            // actually creating COSDictionary with values from predefined CMap.
            String registry = this.getCMapFile().getRegistry();
            String ordering = this.getCMapFile().getOrdering();
            int supplement = this.getCMapFile().getSupplement();
            COSDictionary res = (COSDictionary)
                    COSDictionary.construct(ASAtom.REGISTRY, registry).get();
            res.setStringKey(ASAtom.ORDERING, ordering);
            res.setIntegerKey(ASAtom.SUPPLEMENT, supplement);
            return res;
        }

        if (cidSystemInfo == null) {
            COSObject cidSystemInfoObject = this.getObject().getKey(ASAtom.CID_SYSTEM_INFO);
            if (cidSystemInfoObject.getType() == COSObjType.COS_DICT) {
                this.cidSystemInfo = (COSDictionary) cidSystemInfoObject.getDirectBase();
            } else if (cidSystemInfoObject.getType() == COSObjType.COS_ARRAY) { // see PDF-1.4 specification
                cidSystemInfoObject = cidSystemInfoObject.at(0);
                if (cidSystemInfoObject != null &&
                        cidSystemInfoObject.getType() == COSObjType.COS_DICT) {
                    this.cidSystemInfo = (COSDictionary) cidSystemInfoObject.getDirectBase();
                }
            }
        }
        return this.cidSystemInfo;
    }

    private void parseUseCMap() {
        if (this.useCMap == null) {
            COSObject useCMap = getUseCMap();
            if (!useCMap.empty()) {
                this.useCMap = new PDCMap(useCMap);
            }
        }
    }

    private static ASInputStream loadCMap(String cMapName) {
        try {
            URL resURL = PDCMap.class.getResource(cMapName);
            if (resURL == null) {
                throw new IOException("CMap " + cMapName + " can't be found.");
            }
            File cMapFile = new File(resURL.getFile());
            if (cMapFile.exists()) {
                return new InternalInputStream(cMapFile);
            } else {
                try (InputStream input = PDCMap.class.getResourceAsStream(cMapName)) {
                    return SeekableInputStream.getSeekableStream(input);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Error in opening predefined CMap " + cMapName, e);
            return null;
        }
    }

    /**
     * Gets Unicode value for this code.
     */
    public String toUnicode(int code) {
        String res = null;
        if (this.getCMapFile() != null) {
            res = this.getCMapFile().getUnicode(code);
            if (res == null) {
                parseUseCMap();
                if (useCMap != null) {
                    res = useCMap.toUnicode(code);
                }
            }
        }
        return res;
    }

    /**
     * Checks if this cMap is identity.
     */
    public boolean isIdentity() {
        if (isIdentity == null) {
            isIdentity = this.getCMapName().startsWith("Identity-");
        }
        return isIdentity;
    }
}
