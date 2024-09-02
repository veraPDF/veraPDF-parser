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
package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.util.Collection;
import java.util.Set;

/**
 * @author Timur Kamalov
 */
public class COSIndirect extends COSBase {

    private COSKey key;
    private COSDocument document;
    private COSObject child;

    protected COSIndirect() {
        super();
        this.key = new COSKey();
        this.document = new COSDocument(null);
        this.child = new COSObject();
    }

    protected COSIndirect(final COSKey key, final COSDocument document) {
        super();
        this.key = key;
        this.document = document;
        this.child = new COSObject();
    }

    protected COSIndirect(final COSObject value, final COSDocument document) {
        super();
        this.key = new COSKey();
        this.document = document;
        this.child = new COSObject();

        if (document != null) {
            this.key = this.document.setObject(value);
        }
        this.child = value;
    }

    // Access to base underlying object
    public COSBase get() {
        return this.child.get();
    }

    @Override
    public COSKey getObjectKey() {
        return this.key;
    }

    // OBJECT TYPE
    @Override
    public COSObjType getType() {
        COSObject direct = getDirect();
        return direct == null ? COSObjType.COS_UNDEFINED : direct.getType();
    }

    public static COSObject construct(final COSKey value) {
        return construct(value, null);
    }

    public static COSObject construct(final COSKey value, final COSDocument doc) {
        return new COSObject(new COSIndirect(value, doc));
    }

    public static COSObject construct(final COSObject value) {
        return construct(value, null);
    }

    public static COSObject construct(final COSObject value, final COSDocument doc) {
        return new COSObject(new COSIndirect(value, doc));
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitFromIndirect(this);
    }

    @Override
    public Object accept(final ICOSVisitor visitor) {
        return get() != null ? get().accept(visitor) : COSNull.NULL.accept(visitor);
    }

    //! Boolean values
    @Override
    public Boolean getBoolean() {
        return getDirect().getBoolean();
    }

    @Override
    public boolean setBoolean(final boolean value) {
        getDirect().setBoolean(value);
        return true;
    }

    //! Integer numbers
    @Override
    public Long getInteger() {
        return getDirect().getInteger();
    }

    @Override
    public boolean setInteger(final long value) {
        getDirect().setInteger(value);
        return true;
    }

    //! Real numbers
    @Override
    public Double getReal() {
        return getDirect().getReal();
    }

    @Override
    public boolean setReal(final double value) {
        getDirect().setReal(value);
        return true;
    }

    //! Strings
    @Override
    public String getString() {
        return getDirect().getString();
    }

    @Override
    public boolean setString(final String value) {
        return setString(value, false);
    }

    @Override
    public boolean setString(final String value, final boolean isHex) {
        getDirect().setString(value);
        return true;
    }

    //! Names
    @Override
    public ASAtom getName() {
        return getDirect().getName();
    }

    @Override
    public boolean setName(final ASAtom value) {
        getDirect().setName(value);
        return true;
    }

    //! Number of elements for array and dictionary
    @Override
    public Integer size() {
        return getDirect().size();
    }

    //! Arrays

    @Override
    public COSObject at(final int i) {
        return getDirect().at(i);
    }

    @Override
    public boolean add(final COSObject value) {
        getDirect().add(value);
        return true;
    }

    @Override
    public boolean set(final int i, final COSObject value) {
        getDirect().set(i, value);
        return true;
    }

    @Override
    public boolean insert(final int i, final COSObject value) {
        getDirect().insert(i, value);
        return true;
    }

    @Override
    public void remove(final int i) {
        getDirect().remove(i);
    }

    @Override
    public boolean setArray() {
        getDirect().setArray();
        return true;
    }

    @Override
    public boolean setArray(final int size, final COSObject[] value) {
        getDirect().setArray(size, value);
        return true;
    }

    @Override
    public boolean setArray(final int size, final double[] value) {
        getDirect().setArray(size, value);
        return true;
    }

    @Override
    public void clearArray() {
        getDirect().clear();
    }

    //! Dictionaries
    @Override
    public Boolean knownKey(final ASAtom key) {
        return getDirect().knownKey(key);
    }

    @Override
    public COSObject getKey(final ASAtom key) {
        return getDirect().getKey(key);
    }

    @Override
    public boolean setKey(final ASAtom key, final COSObject value) {
        getDirect().setKey(key, value);
        return true;
    }

    @Override
    public Boolean getBooleanKey(final ASAtom key) {
        return getDirect().getBooleanKey(key);
    }

    @Override
    public boolean setBooleanKey(final ASAtom key, final boolean value) {
        getDirect().setBooleanKey(key, value);
        return true;
    }

    @Override
    public Long getIntegerKey(final ASAtom key) {
        return getDirect().getIntegerKey(key);
    }

    @Override
    public boolean setIntegerKey(final ASAtom key, final long value) {
        getDirect().setIntegerKey(key, value);
        return true;
    }

    @Override
    public Double getRealKey(final ASAtom key) {
        return getDirect().getRealKey(key);
    }

    @Override
    public boolean setRealKey(final ASAtom key, final double value) {
        getDirect().setRealKey(key, value);
        return true;
    }

    @Override
    public String getStringKey(final ASAtom key) {
        return getDirect().getStringKey(key);
    }

    @Override
    public boolean setStringKey(final ASAtom key, final String value) {
        getDirect().setStringKey(key, value);
        return true;
    }

    @Override
    public ASAtom getNameKey(final ASAtom key) {
        return getDirect().getNameKey(key);
    }

    @Override
    public String getNameKeyStringValue(final ASAtom key) {
        return getDirect().getNameKeyStringValue(key);
    }

    @Override
    public String getNameKeyUnicodeValue(final ASAtom key) {
        return getDirect().getNameKeyUnicodeValue(key);
    }

    @Override
    public boolean setNameKey(final ASAtom key, final ASAtom value) {
        getDirect().setNameKey(key, value);
        return true;
    }

    @Override
    public boolean setArrayKey(final ASAtom key) {
        getDirect().setArrayKey(key);
        return true;
    }

    @Override
    public boolean setArrayKey(ASAtom key, COSObject array) {
        getDirect().setArrayKey(key, array);
        return true;
    }

    @Override
    public boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value) {
        getDirect().setArrayKey(key, size, value);
        return true;
    }

    @Override
    public boolean setArrayKey(final ASAtom key, final int size, final double[] value) {
        getDirect().setArrayKey(key, size, value);
        return true;
    }

    @Override
    public void removeKey(final ASAtom key) {
        getDirect().removeKey(key);
    }


    @Override
    public Set<ASAtom> getKeySet() {
        return getDirect().getKeySet();
    }

    @Override
    public Collection<COSObject> getValues() {
        return getDirect().getValues();
    }

    // STREAMS
    @Override
    public ASInputStream getData() {
        return this.getData(COSStream.FilterFlags.RAW_DATA);
    }

    @Override
    public ASInputStream getData(final COSStream.FilterFlags flags) {
        return getDirect().getData(flags);
    }

    @Override
    public boolean setData(final ASInputStream stream) {
        return this.setData(stream, COSStream.FilterFlags.RAW_DATA);
    }

    @Override
    public boolean setData(final ASInputStream stream, final COSStream.FilterFlags flags) {
        getDirect().setData(stream, flags);
        return true;
    }

    @Override
    public Boolean isStreamKeywordCRLFCompliant() {
        return getDirect().isStreamKeywordCRLFCompliant();
    }

    @Override
    public boolean setStreamKeywordCRLFCompliant(final boolean streamKeywordCRLFCompliant) {
        getDirect().setStreamKeywordCRLFCompliant(streamKeywordCRLFCompliant);
        return true;
    }

    @Override
    public Boolean isEndstreamKeywordCRLFCompliant() {
        return getDirect().isEndstreamKeywordCRLFCompliant();
    }

    @Override
    public boolean setEndstreamKeywordCRLFCompliant(final boolean endstreamKeywordCRLFCompliant) {
        getDirect().setEndstreamKeywordCRLFCompliant(endstreamKeywordCRLFCompliant);
        return true;
    }

    @Override
    public Long getRealStreamSize() {
        return getDirect().getRealStreamSize();
    }

    @Override
    public boolean setRealStreamSize(final long realStreamSize) {
        getDirect().setRealStreamSize(realStreamSize);
        return true;
    }

    //! Indirect object
    @Override
    public Boolean isIndirect() {
        return true;
    }

    @Override
    public COSKey getKey() {
        return this.key;
    }

    @Override
    public COSDocument getDocument() {
        return this.document;
    }

    @Override
    public boolean setKey(final COSKey key, final COSDocument document) {
        this.key = key;
        this.document = document;
        return true;
    }

    @Override
    public COSObject getDirect() {
        return this.document != null ? this.document.getObject(key) : this.child;
    }

    @Override
    public COSBase getDirectBase() {
        return this.document != null ? this.document.getObject(key).get() : this.child.get();
    }

    @Override
    public boolean setDirect(final COSObject value) {
        if (this.document != null) {
            this.document.setObject(this.key, value);
        } else {
            this.child = value;
        }
        return true;
    }

    //! Marks object for incremental update.
    //! (If object is indirect and its document is known.)
    @Override
    public void mark() {
        if (this.document != null) {
            COSObject object = new COSObject(this);
            this.document.setObject(object);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSIndirect)) return false;

        COSIndirect that = (COSIndirect) o;

        return this.getDirect().equals(that.getDirect());
    }

    @Override
    public String toString() {
        COSObject direct = getDirect();
        return direct != null ? direct.toString() : null;
    }
}
