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

import org.verapdf.as.io.ASConcatenatedInputStream;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timur Kamalov
 */
public class COSArray extends COSDirect implements Iterable<COSObject> {

    private static final Logger LOGGER = Logger.getLogger(COSArray.class.getCanonicalName());

    private final List<COSObject> entries;

    protected COSArray() {
        super();
        this.entries = new ArrayList<>();
    }

    protected COSArray(final int size, final COSObject[] values) {
        super();
        this.entries = Arrays.asList(values);
    }

    protected COSArray(final int size, final double[] values) {
        super();
        this.entries = new ArrayList<>();
        for (double value : values) {
            this.entries.add(COSReal.construct(value));
        }
    }

    protected COSArray(final int i, final COSObject object) {
        super();
        this.entries = new ArrayList<>();
        this.entries.add(i, object);
    }

    protected COSArray(final int size) {
        super();
        this.entries = new ArrayList<>(size);
    }

    public COSArray(List<COSObject> values) {
        super();
        this.entries = new ArrayList<>();
        this.entries.addAll(values);
    }

    //! Object type
    @Override
    public COSObjType getType() {
        return COSObjType.COS_ARRAY;
    }

    //! Returns COSObject wrapping a new empty COSArray instance
    public static COSObject construct() {
        return new COSObject(new COSArray());
    }

    //! Returns COSObject wrapping a new COSArray instance filled with given values
    public static COSObject construct(final int size, final COSObject[] value) {
        return new COSObject(new COSArray(size, value));
    }

    public static COSObject construct(final int size, final double[] value) {
        return new COSObject(new COSArray(size, value));
    }

    //! Returns COSObject wrapping a new COSArray instance constructed via given object at a given index
    public static COSObject construct(final int i, final COSObject obj) {
        return new COSObject(new COSArray(i, obj));
    }

    public static COSObject construct(final int size) {
        return new COSObject(new COSArray(size));
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitFromArray(this);
    }

    @Override
    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromArray(this);
    }

    @Override
    public Integer size() {
        return this.entries.size();
    }

    //TODO : cosbase?
    @Override
    public Iterator<COSObject> iterator() {
        return this.entries.iterator();
    }

    @Override
    public COSObject at(final int i) {
        if (i >= this.entries.size()) {
            return new COSObject();
        }

        return _at(i);
    }

    @Override
    public boolean add(final COSObject value) {
        this.entries.add(value);
        return true;
    }

    @Override
    public boolean set(final int i, final COSObject value) {
        this.entries.set(i, value);
        return true;
    }

    @Override
    public boolean insert(final int i, final COSObject value) {
        this.entries.add(i, value);
        return true;
    }

    @Override
    public void remove(final int i) {
        if (entries.size() > i) {
            this.entries.remove(i);
        }
    }

    @Override
    public boolean setArray() {
        this.entries.clear();
        return true;
    }

    @Override
    public boolean setArray(final int size, final COSObject[] value) {
        //TODO : check this
        this.entries.addAll(Arrays.asList(value));
        return true;
    }

    @Override
    public boolean setArray(final int size, final double[] values) {
        this.entries.clear();
        for (double value : values) {
            this.entries.add(COSReal.construct(value));
        }
        return true;
    }

    @Override
    public void clearArray() {
        this.entries.clear();
    }

    private COSObject _at(final int i) {
        return this.entries.get(i);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof COSObject) {
            return this.equals(((COSObject) obj).get());
        }
        if (getClass() != obj.getClass()) {
            return false;
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
        if (obj instanceof COSObject) {
            return this.equals(((COSObject) obj).get());
        }
        if (COSBasePair.listContainsPair(checkedObjects, this, (COSBase) obj)) {
            return true;    // Not necessary true, but we should behave as it is
        }
        COSBasePair.addPairToList(checkedObjects, this, (COSBase) obj);
        if (getClass() != obj.getClass()) {
            return false;
        }
        COSArray that = (COSArray) obj;
        if (!that.size().equals(this.size())) {
            return false;
        }
        for (int i = 0; i < this.size(); ++i) {
            COSBase cosBase1 = this.at(i).getDirectBase();
            COSBase cosBase2 = that.at(i).getDirectBase();
            if (!cosBase1.equals(cosBase2, checkedObjects)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ASInputStream getData(final COSStream.FilterFlags flags) {
        List<ASInputStream> streams = new ArrayList<>();
        try {
            for (COSObject object : entries) {
                if (object.getType() == COSObjType.COS_STREAM) {
                    streams.add(object.getData(flags));
                }
            }
        } catch (Exception any) {
            for (ASInputStream stream : streams) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error in closing stream", e);
                }
            }
            throw any;
        }
        return new ASConcatenatedInputStream(streams);
    }

    @Override
    public ASInputStream getData() {
        return getData(COSStream.FilterFlags.RAW_DATA);
    }

    @Override
    public String toString() {
        return entries.toString();
    }
}
