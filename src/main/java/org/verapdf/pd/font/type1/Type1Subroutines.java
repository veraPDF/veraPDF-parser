/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General public license along
 * with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the MPL license along with this program.
 */
package org.verapdf.pd.font.type1;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.font.CFFNumber;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class Type1Subroutines {
    private static final int MAX_SUBR_DEPTH = 100;

    private final SeekableInputStream source;
    private final Map<Integer, Subroutine> subroutines;
    private final Map<Integer, CFFNumber> widths;
    private final Set<Integer> subrsWithoutWidth;
    private final Set<Integer> resolvingSubrs;
    private int depth;

    Type1Subroutines(SeekableInputStream source) {
        this.source = source;
        this.subroutines = new HashMap<>();
        this.widths = new HashMap<>();
        this.subrsWithoutWidth = new HashSet<>();
        this.resolvingSubrs = new HashSet<>();
    }

    void addSubroutine(int number, long offset, long length, int lenIV) {
        subroutines.put(number, new Subroutine(offset, length, lenIV));
    }

    CFFNumber getWidth(int subrNumber) throws IOException {
        CFFNumber width = widths.get(subrNumber);
        if (width != null || subrsWithoutWidth.contains(subrNumber) ||
                resolvingSubrs.contains(subrNumber) || depth >= MAX_SUBR_DEPTH) {
            return width;
        }
        Subroutine subroutine = subroutines.get(subrNumber);
        if (subroutine == null) {
            return null;
        }
        resolvingSubrs.add(subrNumber);
        depth++;
        try {
            Type1CharStringParser parser = new Type1CharStringParser(subroutine.open(source), this);
            width = parser.getWidth();
            if (width != null) {
                widths.put(subrNumber, width);
            } else {
                subrsWithoutWidth.add(subrNumber);
            }
            return width;
        } finally {
            depth--;
            resolvingSubrs.remove(subrNumber);
        }
    }

    static final class Subroutine {
        private final long offset;
        private final long length;
        private final int lenIV;

        Subroutine(long offset, long length, int lenIV) {
            this.offset = offset;
            this.length = length;
            this.lenIV = lenIV;
        }

        private ASInputStream open(SeekableInputStream source) throws IOException {
            return new EexecFilterDecode(source.getStream(offset, length), true, lenIV);
        }
    }
}
