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
import org.verapdf.cos.visitor.ICOSVisitor;
import org.verapdf.cos.visitor.IVisitor;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Timur Kamalov
 */
public class COSName extends COSDirect {

    private ASAtom value;

    protected COSName() {
        super();
    }

    protected COSName(final ASAtom value) {
        super();
        this.value = value;
    }

    protected COSName(final String value) {
        super();
        this.value = ASAtom.getASAtom(value);
    }

    @Override
    public COSObjType getType() {
        return COSObjType.COS_NAME;
    }

    public static COSBase fromValue(final ASAtom value) {
        return new COSName(value);
    }

    public static COSBase fromValue(final String value) {
        return new COSName(value);
    }

    public static COSObject construct(final ASAtom value) {
        return new COSObject(new COSName(value));
    }

    public static COSObject construct(final String value) {
        return new COSObject(new COSName(value));
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitFromName(this);
    }

    @Override
    public Object accept(final ICOSVisitor visitor) {
        return visitor.visitFromName(this);
    }

    //! ASAtom data exchange
    @Override
    public ASAtom getName() {
        return this.value;
    }

    @Override
    public boolean setName(final ASAtom value) {
        set(value);
        return true;
    }

    public ASAtom get() {
        return value;
    }

    public void set(final ASAtom value) {
        this.value = value;
    }

    public void set(final String value) {
        this.value = ASAtom.getASAtom(value);
    }

    //! String data exchange
		/*! It is recommended to use ASAtom instead of the string representation of the PDF Name object
		    whenever possible. See method GetName().
		*/
    @Override
    public String getString() {
        return this.value.getValue();
    }

    public String getUnicodeValue() {
        return new String(this.getString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    @Override
    public boolean setString(final String value) {
        set(value);
        return true;
    }

    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSName)) return false;

        COSName cosName = (COSName) o;

        return Objects.equals(value, cosName.value);

    }
}
