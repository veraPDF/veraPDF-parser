package org.verapdf.pd.font.cmap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class represents identity CMap.
 *
 * @author Sergey Shemyakov
 */
public class IdentityCMap extends CMap {

    private static final String ADOBE = "Adobe";
    private static final String IDENTITY = "Identity";
    private static final int DEFAULT_SUPPLEMENT = 0;

    public IdentityCMap(String name) {
        this.setName(name);
    }

    @Override
    public int toCID(int character) {
        return character;
    }

    @Override
    public boolean containsCode(int character) {
        return character != 0;
    }

    @Override
    public int getCodeFromStream(InputStream stream) throws IOException {
        int firstByte = stream.read();
        int secondByte = stream.read();
        return (firstByte << 8) + secondByte;
    }

    @Override
    public String getRegistry() {
        return ADOBE;
    }

    @Override
    public String getOrdering() {
        return IDENTITY;
    }

    @Override
    public int getSupplement() {
        return DEFAULT_SUPPLEMENT;
    }

    @Override
    public String getUnicode(int code) {
        return null;
    }
}
