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
package org.verapdf.tools.resource;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that handles resource closing.
 *
 * @author Sergey Shemyakov
 */
public class FileResourceHandler implements Closeable {

    private final Set<Closeable> resources;

    public FileResourceHandler() {
        this.resources = new HashSet<>();
    }

    /**
     * Adds resource for closing.
     *
     * @param obj is a file stream closer to be stored.
     */
    public void addResource(ASFileStreamCloser obj) {
        if (obj != null) {
            Closeable resource = obj.getStream();
            if (resource != null) {
                resources.add(obj);
            }
        }
    }

    /**
     * Adds resource for closing.
     *
     * @param res is a closeable object to be stored.
     */
    public void addResource(Closeable res) {
        if (res != null) {
            resources.add(res);
        }
    }

    /**
     * Closes all stored resources.
     */
    @Override
    public void close() throws IOException {
        for (Closeable obj : resources) {
            obj.close();
        }
    }

    /**
     * Adds all closeable objects from given list to handler.
     * @param resources
     */
    public void addAll(List<Closeable> resources) {
        this.resources.addAll(resources);
    }
}
