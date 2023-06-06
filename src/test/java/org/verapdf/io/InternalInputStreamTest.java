/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2023, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.verapdf.as.io.ASMemoryInStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class InternalInputStreamTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void substreamOfStreamAtOffsetShouldReportCorrectOffset() throws IOException {
        byte[] buf = new byte[] { 0 };
        File file = temporaryFolder.newFile();
        Files.write(file.toPath(), new byte[] { 1, 2, 3 });

        try (InternalInputStream stream = InternalInputStream.createConcatenated(buf, Files.newInputStream(file.toPath()))) {
            assertEquals(0, stream.getOffset());
        }
    }
}
