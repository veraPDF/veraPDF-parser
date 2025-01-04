/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.cos;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;

/**
 * @author Timur Kamalov
 */
public abstract class COSDirect extends COSBase {

    private static final Logger LOGGER = Logger.getLogger(COSDirect.class.getCanonicalName());

    private static final String INVALID_TYPE_MESSAGE = "Invalid object type. Expected %s got %s.";
    private static final String COS_DICTIONARY_TYPE = "COSDictionary";
    private static final String COS_STREAM_TYPE = "COSStream";
    private static final String COS_INDIRECT_TYPE = "COSIndirect";


    // OBJECT TYPE
    @Override
    public COSObjType getType() {
        return COSObjType.COS_UNDEFINED;
    }

    // BOOLEAN VALUES
    @Override
    public Boolean getBoolean() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, "COSBoolean", getClass().toString()));
        return null;
    }

    @Override
    public boolean setBoolean(final boolean value) {
        return false;
    }

    // INTEGER NUMBERS
    @Override
    public Long getInteger() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, "COSInteger", getClass().toString()));
        return null;
    }

    @Override
    public boolean setInteger(final long value) {
        return false;
    }

    // REAL NUMBERS
    @Override
    public Double getReal() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, "COSReal", getClass().toString()));
        return null;
    }

    @Override
    public boolean setReal(final double value) {
        return false;
    }

    // STRINGS
    @Override
    public String getString() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, "COSString", getClass().toString()));
        return null;
    }

    @Override
    public boolean setString(final String value) {
        return setString(value, false);
    }

    @Override
    public boolean setString(final String value, final boolean isHex) {
        return false;
    }

    // NAMES
    @Override
    public ASAtom getName() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, "COSName", getClass().toString()));
        return null;
    }

    @Override
    public boolean setName(final ASAtom value) {
        return false;
    }

    // NUMBERS OF ELEMENTS FOR ARRAY AND DICTIONARY
    @Override
    public Integer size() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, "COSInteger", getClass().toString()));
        return null;
    }

    // ARRAYS
    @Override
    public COSObject at(final int i) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, "COSArray", getClass().toString()));
        return null;
    }

    @Override
    public boolean add(final COSObject value) {
        return false;
    }

    @Override
    public boolean set(final int i, final COSObject value) {
        return false;
    }

    @Override
    public boolean insert(final int i, final COSObject value) {
        return false;
    }

    @Override
    public void remove(final int i) {
    }

    @Override
    public boolean setArray() {
        return false;
    }

    @Override
    public boolean setArray(final int size, final COSObject[] value) {
        return false;
    }

    @Override
    public boolean setArray(final int size, final double[] value) {
        return false;
    }

    @Override
    public void clearArray() {
    }

    // DICTIONARIES
    @Override
    public Boolean knownKey(final ASAtom key) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public COSObject getKey(final ASAtom key) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setKey(final ASAtom key, final COSObject value) {
        return false;
    }

    @Override
    public Boolean getBooleanKey(final ASAtom key) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setBooleanKey(final ASAtom key, final boolean value) {
        return false;
    }

    @Override
    public Long getIntegerKey(final ASAtom key) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setIntegerKey(final ASAtom key, final long value) {
        return false;
    }

    @Override
    public Double getRealKey(final ASAtom key) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setRealKey(final ASAtom key, final double value) {
        return false;
    }

    @Override
    public String getStringKey(final ASAtom key) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setStringKey(final ASAtom key, final String value) {
        return false;
    }

    @Override
    public ASAtom getNameKey(final ASAtom key) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public String getNameKeyStringValue(final ASAtom key) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public String getNameKeyUnicodeValue(final ASAtom key) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setNameKey(final ASAtom key, final ASAtom value) {
        return false;
    }

    @Override
    public boolean setArrayKey(final ASAtom key) {
        return false;
    }

    @Override
    public boolean setArrayKey(ASAtom key, COSObject array) {
        return false;
    }

    @Override
    public boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value) {
        return false;
    }

    @Override
    public boolean setArrayKey(final ASAtom key, final int size, final double[] value) {
        return false;
    }

    @Override
    public void removeKey(final ASAtom key) {
    }

    @Override
    public Set<ASAtom> getKeySet() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public Collection<COSObject> getValues() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_DICTIONARY_TYPE, getClass().toString()));
        return null;
    }

    // STREAMS
    @Override
    public ASInputStream getData() {
        return this.getData(COSStream.FilterFlags.RAW_DATA);
    }

    @Override
    public ASInputStream getData(final COSStream.FilterFlags flags) {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_STREAM_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setData(final ASInputStream stream) {
        return this.setData(stream, COSStream.FilterFlags.RAW_DATA);
    }

    @Override
    public boolean setData(final ASInputStream stream, final COSStream.FilterFlags flags) {
        return false;
    }

    @Override
    public Boolean isStreamKeywordCRLFCompliant() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_STREAM_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setStreamKeywordCRLFCompliant(final boolean streamKeywordCRLFCompliant) {
        return false;
    }

    @Override
    public Boolean isEndstreamKeywordCRLFCompliant() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_STREAM_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setEndstreamKeywordCRLFCompliant(final boolean endstreamKeywordCRLFCompliant) {
        return false;
    }

    @Override
    public Long getRealStreamSize() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_STREAM_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setRealStreamSize(final long realStreamSize) {
        return false;
    }

    // INDIRECT OBJECT
    @Override
    public Boolean isIndirect() {
        return Boolean.FALSE;
    }

    @Override
    public COSKey getKey() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_INDIRECT_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public COSDocument getDocument() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_INDIRECT_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public boolean setKey(final COSKey key, final COSDocument document) {
        return false;
    }

    @Override
    public COSObject getDirect() {
        LOGGER.log(Level.FINE, String.format(INVALID_TYPE_MESSAGE, COS_INDIRECT_TYPE, getClass().toString()));
        return null;
    }

    @Override
    public COSBase getDirectBase() {
        return this;
    }

    @Override
    public boolean setDirect(final COSObject value) {
        return false;
    }

    //! Marks object for incremental update.
    //! (If object is indirect and its document is known.)
    @Override
    public void mark() {
    }

}
