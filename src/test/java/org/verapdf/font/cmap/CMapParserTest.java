package org.verapdf.font.cmap;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Sergey Shemyakov
 */
public class CMapParserTest {
    private String cMapPath = "src/test/resources/org/verapdf/font/cmap/83pv-RKSJ-H";

    @Test
    public void test() {
        try {
            CMapParser parser = new CMapParser(cMapPath);
            parser.parse();
            CMap cMap = parser.getCMap();
            assertTrue(cMap.getRegistry().equals("Adobe"));
            assertTrue(cMap.getOrdering().equals("Japan1"));
            assertTrue(cMap.toCID(0x1F) == 1);
            assertTrue(cMap.toCID(0x84bc) == 7516);
            assertTrue(cMap.toCID(0xECEE) == 921);
            assertTrue(cMap.toCID(0xECEF) == 922);
        } catch (FileNotFoundException ex) {
            System.out.println("File " + cMapPath + " not found.");
        } catch (IOException ex) {
            System.out.println("Parsing error: ");
            ex.printStackTrace();
        }
    }
}
