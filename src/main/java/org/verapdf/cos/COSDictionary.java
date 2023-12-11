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
package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.util.*;

/**
 * @author Timur Kamalov
 */
public class COSDictionary extends COSDirect {

    public static final String SIZE = "size";
    public static final String DICTIONARY = "dictionary";

    private final Map<ASAtom, COSObject> entries;

    protected COSDictionary() {
        super();
        this.entries = new HashMap<>();
    }

    protected COSDictionary(final ASAtom key, final COSObject value) {
        this();
        setKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final boolean value) {
        this();
        setBooleanKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final int value) {
        this();
        setIntegerKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final double value) {
        this();
        setRealKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final String value) {
        this();
        setStringKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final ASAtom value) {
        this();
        setNameKey(key, value);
    }

    protected COSDictionary(final ASAtom key, final int size, final COSObject[] value) {
        this();
        setArrayKey(key, size, value);
    }

    protected COSDictionary(final ASAtom key, final int size, final double[] value) {
        this();
        setArrayKey(key, size, value);
    }

    protected COSDictionary(final COSDictionary dict) {
        super();
        this.entries = dict.entries;
    }

    //! Object type
    @Override
    public COSObjType getType() {
        return COSObjType.COS_DICT;
    }

    public static COSObject construct() {
        return new COSObject(new COSDictionary());
    }

    public static COSObject construct(final ASAtom key, final COSObject value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final boolean value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final int value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final double value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final String value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final ASAtom value) {
        return new COSObject(new COSDictionary(key, value));
    }

    public static COSObject construct(final ASAtom key, final int size, final COSObject[] value) {
        return new COSObject(new COSDictionary(key, size, value));
    }

    public static COSObject construct(final ASAtom key, final int size, final double[] value) {
        return new COSObject(new COSDictionary(key, size, value));
    }

    public static COSObject construct(final COSDictionary dict) {
        return new COSObject(new COSDictionary(dict));
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitFromDictionary(this);
    }

    @Override
    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromDictionary(this);
    }

    @Override
    public Integer size() {
        return this.entries.size();
    }

    @Override
    public Boolean knownKey(final ASAtom key) {
        return this.entries.containsKey(key);
    }

    @Override
    public COSObject getKey(final ASAtom key) {
        COSObject value = this.entries.get(key);
        return value != null ? value : new COSObject();
    }

    @Override
    public boolean setKey(final ASAtom key, final COSObject value) {
        if (value.empty() || value.get() instanceof COSNull) {
            this.entries.remove(key);
        } else {
            this.entries.put(key, value);
        }
        return true;
    }

    @Override
    public Boolean getBooleanKey(final ASAtom key) {
        return getKey(key).getBoolean();
    }

    @Override
    public boolean setBooleanKey(final ASAtom key, final boolean value) {
        COSObject obj = new COSObject();
        obj.setBoolean(value);
        this.entries.put(key, obj);
        return true;
    }

    @Override
    public Long getIntegerKey(final ASAtom key) {
        return getKey(key).getInteger();
    }

    @Override
    public boolean setIntegerKey(final ASAtom key, final long value) {
        COSObject obj = new COSObject();
        obj.setInteger(value);
        this.entries.put(key, obj);
        return true;
    }

    @Override
    public Double getRealKey(final ASAtom key) {
        return getKey(key).getReal();
    }

    @Override
    public boolean setRealKey(final ASAtom key, final double value) {
        COSObject obj = new COSObject();
        obj.setReal(value);
        this.entries.put(key, obj);
        return true;
    }

    @Override
    public String getStringKey(final ASAtom key) {
        COSObject object = getKey(key);
        if (object.getType() == COSObjType.COS_NAME) {
            return null;
        }
        return object.getString();
    }

    @Override
    public boolean setStringKey(final ASAtom key, final String value) {
        COSObject obj = new COSObject();
        obj.setString(value);
        this.entries.put(key, obj);
        return true;
    }

    @Override
    public final ASAtom getNameKey(final ASAtom key) {
        return getKey(key).getName();
    }

    @Override
    public final String getNameKeyStringValue(final ASAtom key) {
        ASAtom value = getNameKey(key);
        return value != null ? value.getValue() : null;
    }

    @Override
    public boolean setNameKey(final ASAtom key, final ASAtom value) {
        COSObject obj = new COSObject();
        obj.setName(value);
        this.entries.put(key, obj);
        return true;
    }

    @Override
    public boolean setArrayKey(final ASAtom key) {
        COSObject obj = new COSObject();
        obj.setArray();
        this.entries.put(key, obj);
        return true;
    }

    @Override
    public boolean setArrayKey(final ASAtom key, final COSObject array) {
        this.entries.put(key, array);
        return true;
    }

    @Override
    public boolean setArrayKey(final ASAtom key, final int size, final COSObject[] value) {
        COSObject obj = new COSObject();
        obj.setArray(size, value);
        this.entries.put(key, obj);
        return true;
    }

    @Override
    public boolean setArrayKey(final ASAtom key, final int size, final double[] value) {
        COSObject obj = new COSObject();
        obj.setArray(size, value);
        this.entries.put(key, obj);
        return true;
    }

    @Override
    public void removeKey(final ASAtom key) {
        this.entries.remove(key);
    }

    // Instead of iterator
    public Set<Map.Entry<ASAtom, COSObject>> getEntrySet() {
        return this.entries.entrySet();
    }

    @Override
    public Set<ASAtom> getKeySet() {
        return this.entries.keySet();
    }

    @Override
    public Collection<COSObject> getValues() {
        return this.entries.values();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(obj instanceof COSObject) {
            return this.equals(((COSObject) obj).get());
        }
        List<COSBasePair> checkedObjects = new LinkedList<>();
        return this.equals(obj, checkedObjects);
    }

    @Override
    boolean equals(Object obj, List<COSBasePair> checkedObjects) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if(obj instanceof COSObject) {
            return this.equals(((COSObject) obj).get());
        }
        if (COSBasePair.listContainsPair(checkedObjects, this, (COSBase) obj)) {
            return true;    // Not necessary true, but we should behave as it is
        }
        COSBasePair.addPairToList(checkedObjects, this, (COSBase) obj);
        if (getClass() != obj.getClass()) {
            return false;
        }
        COSDictionary that = (COSDictionary) obj;
        if (!that.size().equals(this.size())) {
            return false;
        }
        Set<ASAtom> set1 = this.getKeySet();
        if (!set1.equals(that.getKeySet())) {
            return false;
        }
        for (ASAtom name : set1) {
            COSBase cosBase1 = this.getKey(name).get();
            COSBase cosBase2 = that.getKey(name).get();
            if (!cosBase1.equals(cosBase2, checkedObjects)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return DICTIONARY + '(' + SIZE + " = " + size() + ')';
    }

}
