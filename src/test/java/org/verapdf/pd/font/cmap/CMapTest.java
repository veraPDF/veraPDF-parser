/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2024, veraPDF Consortium <info@verapdf.org>
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

import org.junit.Test;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.io.InternalInputStream;
import org.verapdf.parser.postscript.PostScriptException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Shemyakov
 */
public class CMapTest {
    private final String cMapPath = "src/test/resources/org/verapdf/pd/font/cmap/83pv-RKSJ-H";

    private final byte[] begin1 = {0x40, 0x40, 0x40};
    private final byte[] end1 = {(byte) 0x80, (byte) 0x80, (byte) 0x80};
    private final byte[] begin2 = {(byte) 0x81, 0x40};
    private final byte[] end2 = {(byte) 0x90, (byte) 0x80};
    private final byte[] begin3 = {0x40, 0x40, (byte) 0x81, 0x20};
    private final byte[] end3 = {(byte) 0x80, (byte) 0x80, (byte) 0xA0, 0x40};

    @Test
    public void testParser() {
        try {
            InternalInputStream stream = new InternalInputStream(cMapPath, 2);
            CMapParser parser = new CMapParser(stream);
            parser.parse();
            CMap cMap = parser.getCMap();
            assertEquals("Adobe", cMap.getRegistry());
            assertEquals("Japan1", cMap.getOrdering());
            assertEquals(1, cMap.toCID(0x1F));
            assertEquals(7516, cMap.toCID(0x84bc));
            assertEquals(921, cMap.toCID(0xECEE));
            assertEquals(922, cMap.toCID(0xECEF));
        } catch (FileNotFoundException ex) {
            System.out.println("File " + cMapPath + " not found.");
        } catch (IOException ex) {
            System.out.println("Parsing error: ");
            ex.printStackTrace();
        } catch (PostScriptException e) {
            System.out.println("PostScript parsing exception :");
            e.printStackTrace();
        }
    }

    @Test
    public void testCMap() {
        try {
            InternalInputStream internalInputStream = new InternalInputStream(cMapPath, 2);
            CMapParser parser = new CMapParser(internalInputStream);
            parser.parse();
            CMap cMap = parser.getCMap();   //Testing defined characters
            byte[] bytes = {0x01, (byte) 0x81, 0x40, (byte) 0xFE, (byte) 0xE6, (byte) 0x4A};
            ASInputStream stream = new ASMemoryInStream(bytes);
            assertEquals(1, cMap.toCID(cMap.getCodeFromStream(stream)));
            assertEquals(633, cMap.toCID(cMap.getCodeFromStream(stream)));
            assertEquals(228, cMap.toCID(cMap.getCodeFromStream(stream)));
            assertEquals(6638, cMap.toCID(cMap.getCodeFromStream(stream)));

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
            assertEquals(0, cMap1.getCodeFromStream(stream1));
            assertEquals(9, stream1.available());

            assertEquals(0, cMap1.getCodeFromStream(stream1));
            assertEquals(6, stream1.available());

            assertEquals(0, cMap1.getCodeFromStream(stream1));
            assertEquals(2, stream1.available());

            assertEquals(0, cMap1.getCodeFromStream(stream1));
            assertEquals(0, stream1.available());
        } catch (FileNotFoundException ex) {
            System.out.println("File " + cMapPath + " not found.");
        } catch (IOException ex) {
            System.out.println("Parsing error: ");
            ex.printStackTrace();
        } catch (PostScriptException e) {
            System.out.println("PostScript parsing exception :");
            e.printStackTrace();
        }
    }
}
