package org.verapdf.font.cmap;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.parser.BaseParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class parses CMap files and constructs CMap objects.
 * @author Sergey Shemyakov
 */
public class CMapParser extends BaseParser{

    CMap cMap;

    /**
     * {@inheritDoc}
     */
    public CMapParser(String fileName) throws FileNotFoundException {
        super(fileName);
        cMap = new CMap();
    }

    /**
     * {@inheritDoc}
     */
    public CMapParser(InputStream fileStream) throws IOException {
        super(fileStream);
        cMap = new CMap();
    }

    /**
     * {@inheritDoc}
     */
    public CMapParser(ASInputStream asInputStream) throws IOException {
        super(asInputStream);
        cMap = new CMap();
    }

    /**
     * @return constructed CMap.
     */
    public CMap getCMap() {
        return cMap;
    }

    /**
     * Method parses
     */
    public void parse() throws IOException {
        //Skipping starting comments
        skipSpaces(true);

        nextToken();

        switch (getToken().token) {
            case "WMode":
        }
    }
}
