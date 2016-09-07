package org.verapdf.pd.font.cmap;

import org.junit.Test;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.ASMemoryInStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Sergey Shemyakov
 */
public class CMapTest {
    private String cMapPath = "src/test/resources/org/verapdf/pd/font/cmap/83pv-RKSJ-H";

    @Test
    public void testParser() {
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

    private byte[] begin1 = {0x40, 0x40, 0x40};
    private byte[] end1 = {(byte) 0x80, (byte) 0x80, (byte) 0x80};
    private byte[] begin2 = {(byte) 0x81, 0x40};
    private byte[] end2 = {(byte) 0x90, (byte) 0x80};
    private byte[] begin3 = {0x40, 0x40, (byte) 0x81, 0x20};
    private byte[] end3 = {(byte) 0x80, (byte) 0x80, (byte) 0xA0, 0x40};

    @Test
    public void testCMap() {
        try {
            CMapParser parser = new CMapParser(cMapPath);
            parser.parse();
            CMap cMap = parser.getCMap();   //Testing defined characters
            byte[] bytes = {0x01, (byte) 0x81, 0x40, (byte) 0xFE, (byte) 0xE6, (byte) 0x4A};
            ASInputStream stream = new ASMemoryInStream(bytes);
            assertTrue(cMap.getCIDFromStream(stream) == 1);
            assertTrue(cMap.getCIDFromStream(stream) == 633);
            assertTrue(cMap.getCIDFromStream(stream) == 228);
            assertTrue(cMap.getCIDFromStream(stream) == 6638);

            CMap cMap1 = new CMap();    //Testing undefined characters
            List<CodeSpace> list = new ArrayList<>(3);
            list.add(new CodeSpace(begin1, end1));
            list.add(new CodeSpace(begin2, end2));
            list.add(new CodeSpace(begin3, end3));
            cMap1.setCodeSpaces(list);
            cMap1.shortestCodeSpaceLength = 2;
            byte[] bytes1 = {0x15, (byte) 0xFF,/*that should be read firstly*/
                    0x41, 0x41, (byte) 0xFF, /*that should be read secondly*/
                    0x41, 0x41, (byte) 0x90, (byte) 0xFF, /*that should be read thirdly*/
                    (byte) 0x82, (byte) 0xFF /*that should be read fourthly*/};
            ASMemoryInStream stream1 = new ASMemoryInStream(bytes1);
            assertTrue(cMap1.getCIDFromStream(stream1) == 0);
            assertTrue(stream1.available() == 9);

            assertTrue(cMap1.getCIDFromStream(stream1) == 0);
            assertTrue(stream1.available() == 6);

            assertTrue(cMap1.getCIDFromStream(stream1) == 0);
            assertTrue(stream1.available() == 2);

            assertTrue(cMap1.getCIDFromStream(stream1) == 0);
            assertTrue(stream1.available() == 0);
        } catch (FileNotFoundException ex) {
            System.out.println("File " + cMapPath + " not found.");
        } catch (IOException ex) {
            System.out.println("Parsing error: ");
            ex.printStackTrace();
        }
    }
}
