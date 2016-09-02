package org.verapdf.pd.font.cmap;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents CMap on PD layer.
 *
 * @author Sergey Shemyakov
 */
public class PDCMap {

    private COSObject cMap;
    private COSDictionary cidSystemInfo;

    /**
     * Constructor from COSObject.
     *
     * @param cMap is COSStream containing CMap or COSName containing name of
     *             predefined CMap.
     */
    public PDCMap(COSObject cMap) {
        this.cMap = cMap == null ? COSObject.getEmpty() : cMap;
    }

    /**
     * @return name of this CMap.
     */
    public String getCMapName() {
        if (this.cMap.getType() == COSObjType.COS_NAME) {
            return cMap.getString();
        }
        if (this.cMap.getType() == COSObjType.COS_STREAM) {
            COSObject cMapName = this.cMap.getKey(ASAtom.CMAPNAME);
            if (cMapName != COSObject.getEmpty()) {
                return cMapName.getString();
            }
        }
        return "";
    }

    /**
     * @return COSObject, representing this CMap.
     */
    public COSObject getcMap() {
        return cMap;
    }

    /**
     * @return CMap file object read from stream or loaded from predefined CMap
     * file.
     * @throws IOException if CMap cannot be read.
     */
    public CMap getCMapFile() throws IOException {
        if (this.cMap.getType() == COSObjType.COS_STREAM) {
            CMapParser parser =
                    new CMapParser(this.cMap.getData(COSStream.FilterFlags.DECODE));
            parser.parse();
            return parser.getCMap();
        } else if (this.cMap.getType() == COSObjType.COS_NAME) {
            String name = this.cMap.getString();
            String cMapPath = "/font/cmap/" + name;
            try {
                File cMap = new File(getSystemIndependentPath(cMapPath));
                InputStream cMapStream = new FileInputStream(cMap);
                CMapParser parser = new CMapParser(cMapStream);
                parser.parse();
                return parser.getCMap();
            } catch (URISyntaxException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @return Registry value from CMap CIDSystemInfo dictionary.
     */
    public String getRegistry() {
        if(this.getCIDSystemInfo() == null) {
            return null;
        } else {
            return this.getCIDSystemInfo().getStringKey(ASAtom.REGISTRY);
        }
    }

    /**
     * @return Ordering value from CMap CIDSystemInfo dictionary.
     */
    public String getOrdering() {
        if(this.getCIDSystemInfo() == null) {
            return null;
        } else {
            return this.getCIDSystemInfo().getStringKey(ASAtom.ORDERING);
        }
    }

    /**
     * @return Supplement value from CMap CIDSystemInfo dictionary.
     */
    public Long getSupplement() {
        if(this.getCIDSystemInfo() == null) {
            return null;
        } else {
            return this.getCIDSystemInfo().getIntegerKey(ASAtom.SUPPLEMENT);
        }
    }

    private COSDictionary getCIDSystemInfo() {
        if (cidSystemInfo == null) {
            this.cidSystemInfo = (COSDictionary)
                    this.cMap.getKey(ASAtom.CID_SYSTEM_INFO).get();
            return this.cidSystemInfo;
        } else {
            return this.cidSystemInfo;
        }
    }

    private static String getSystemIndependentPath(String path)
            throws URISyntaxException {
        URL resourceUrl = ClassLoader.class.getResource(path);
        Path resourcePath = Paths.get(resourceUrl.toURI());
        return resourcePath.toString();
    }
}
