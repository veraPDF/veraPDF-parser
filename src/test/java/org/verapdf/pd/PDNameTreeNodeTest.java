/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd;

import org.junit.Test;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSInteger;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSString;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PDNameTreeNodeTest {

    @Test
    public void returnsValueForEachKeyInNames() {
        PDNameTreeNode node = PDNameTreeNode.create(treeNode(
                array(literal("Alpha"), literal("Gamma")),
                array(literal("Alpha"), integer(1), literal("Beta"), integer(2), literal("Gamma"), integer(3)),
                null));

        assertValue(1, node.getObject(key("Alpha")));
        assertValue(2, node.getObject(key("Beta")));
        assertValue(3, node.getObject(key("Gamma")));
    }

    @Test
    public void searchesKeysEqualToLimits() {
        PDNameTreeNode node = PDNameTreeNode.create(treeNode(
                array(literal("Alpha"), literal("Gamma")),
                array(literal("Alpha"), integer(1), literal("Gamma"), integer(3)),
                null));

        assertValue(1, node.getObject(key("Alpha")));
        assertValue(3, node.getObject(key("Gamma")));
    }

    @Test
    public void returnsNullForKeyMissingWithinLimits() {
        PDNameTreeNode node = PDNameTreeNode.create(treeNode(
                array(literal("Alpha"), literal("Gamma")),
                array(literal("Alpha"), integer(1), literal("Gamma"), integer(3)),
                null));

        assertNull(node.getObject(key("Bravo")));
    }

    @Test
    public void returnsNullForKeysOutsideLimits() {
        PDNameTreeNode node = PDNameTreeNode.create(treeNode(
                array(literal("Beta"), literal("Gamma")),
                array(literal("Beta"), integer(2), literal("Gamma"), integer(3)),
                null));

        assertNull(node.getObject(key("Alpha")));
        assertNull(node.getObject(key("Zulu")));
    }

    @Test
    public void findsKeyAcrossLiteralAndHexEncoding() {
        PDNameTreeNode literalNode = PDNameTreeNode.create(treeNode(
                null, array(literal("Beta"), integer(2)), null));
        PDNameTreeNode hexNode = PDNameTreeNode.create(treeNode(
                null, array(hex("Beta"), integer(2)), null));

        assertValue(2, literalNode.getObject(hexKey("Beta")));
        assertValue(2, hexNode.getObject(key("Beta")));
    }

    @Test
    public void comparesKeysAsUnsignedBytes() {
        // Byte 0xE9 is negative as a signed byte, so this checks that keys compare as unsigned bytes
        byte[] highKey = {(byte) 0xE9};
        PDNameTreeNode node = PDNameTreeNode.create(treeNode(
                array(literal("Alpha"), COSString.construct(highKey, false)),
                array(literal("Alpha"), integer(1), COSString.construct(highKey, false), integer(2)),
                null));

        assertValue(2, node.getObject(new COSString(highKey, false)));
    }

    @Test
    public void searchesNamesWhenNodeHasNoLimits() {
        PDNameTreeNode node = PDNameTreeNode.create(treeNode(
                null, array(literal("Alpha"), integer(1), literal("Beta"), integer(2)), null));

        assertValue(1, node.getObject(key("Alpha")));
        assertValue(2, node.getObject(key("Beta")));
        assertNull(node.getObject(key("Zulu")));
    }

    @Test
    public void returnsNullWhenNodeHasNoNamesOrKids() {
        PDNameTreeNode node = PDNameTreeNode.create(treeNode(
                array(literal("Alpha"), literal("Gamma")), null, null));

        assertNull(node.getObject(key("Beta")));
    }

    @Test
    public void findsStringKeysAfterNonStringKeyInNames() {
        PDNameTreeNode node = PDNameTreeNode.create(treeNode(
                null, array(integer(7), integer(10), literal("Beta"), integer(2)), null));

        assertValue(2, node.getObject(key("Beta")));
    }

    @Test
    public void searchesKidsForKey() {
        PDNameTreeNode root = PDNameTreeNode.create(twoLeafTree());

        assertValue(1, root.getObject(key("Alpha")));
        assertValue(2, root.getObject(key("Beta")));
        assertValue(3, root.getObject(key("Gamma")));
        assertValue(4, root.getObject(key("Delta")));
    }

    @Test
    public void returnsNullWhenKeyIsNotInAnyKid() {
        PDNameTreeNode root = PDNameTreeNode.create(twoLeafTree());

        // Bravo lies inside the root limits but in the gap between the two kids
        assertNull(root.getObject(key("Bravo")));
        assertNull(root.getObject(key("Zulu")));
    }

    private static COSObject twoLeafTree() {
        return treeNode(
                array(literal("Alpha"), literal("Gamma")),
                null,
                array(
                        treeNode(array(literal("Alpha"), literal("Beta")),
                                array(literal("Alpha"), integer(1), literal("Beta"), integer(2)), null),
                        treeNode(array(literal("Delta"), literal("Gamma")),
                                array(literal("Gamma"), integer(3), literal("Delta"), integer(4)), null)));
    }

    private static COSObject treeNode(COSObject limits, COSObject names, COSObject kids) {
        COSObject tree = COSDictionary.construct();
        if (limits != null) {
            tree.setKey(ASAtom.LIMITS, limits);
        }
        if (names != null) {
            tree.setKey(ASAtom.NAMES, names);
        }
        if (kids != null) {
            tree.setKey(ASAtom.KIDS, kids);
        }
        return tree;
    }

    private static COSObject array(COSObject... items) {
        COSObject result = COSArray.construct();
        for (COSObject item : items) {
            result.add(item);
        }
        return result;
    }

    private static COSObject literal(String value) {
        return COSString.construct(bytes(value), false);
    }

    private static COSObject hex(String value) {
        return COSString.construct(bytes(value), true);
    }

    private static COSObject integer(long value) {
        return COSInteger.construct(value);
    }

    private static COSString key(String value) {
        return new COSString(bytes(value), false);
    }

    private static COSString hexKey(String value) {
        return new COSString(bytes(value), true);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static void assertValue(long expected, COSObject actual) {
        assertNotNull(actual);
        assertEquals(Long.valueOf(expected), actual.getInteger());
    }
}
