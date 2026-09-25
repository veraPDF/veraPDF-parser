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
package org.verapdf.io;

import java.io.File;
import java.io.IOException;

/**
 * Central creation point for the temporary files that the parser writes while turning non-seekable
 * input (embedded font programs, CMaps, decoded object streams, incremental-update output) into
 * seekable data.
 *
 * <p>By default this behaves exactly like {@link File#createTempFile(String, String)} and writes into
 * the JVM temporary directory ({@code java.io.tmpdir}). A caller that needs the temporary files in a
 * specific directory - for example one directory per worker so files stay inside an isolated working
 * area and can be removed deterministically - can configure a target directory. Two levels are offered:
 * a process-wide default and a per-thread override that takes precedence. Both are optional; when
 * neither is set the historical behaviour is preserved, so this change is backward compatible.</p>
 *
 * <p>The per-thread override is the natural fit for a server that validates one document per worker
 * thread: set it before parsing, {@link #clearTempDirectory() clear} it afterwards.</p>
 */
public final class TempFileHandler {

	private static volatile File defaultTempDirectory;

	private static final ThreadLocal<File> TEMP_DIRECTORY = new ThreadLocal<>();

	private TempFileHandler() {
	}

	/**
	 * Sets the process-wide default directory for parser temporary files. {@code null} restores the
	 * JVM default ({@code java.io.tmpdir}).
	 *
	 * @param directory target directory, or {@code null} for the JVM default
	 */
	public static void setDefaultTempDirectory(File directory) {
		defaultTempDirectory = directory;
	}

	/**
	 * @return the process-wide default directory, or {@code null} if none is configured
	 */
	public static File getDefaultTempDirectory() {
		return defaultTempDirectory;
	}

	/**
	 * Sets the temporary-file directory for the current thread only. It takes precedence over the
	 * process-wide default and should be cleared when the thread is done ({@link #clearTempDirectory()}).
	 *
	 * @param directory target directory for this thread, or {@code null} to fall back to the default
	 */
	public static void setTempDirectory(File directory) {
		if (directory == null) {
			TEMP_DIRECTORY.remove();
		} else {
			TEMP_DIRECTORY.set(directory);
		}
	}

	/**
	 * Removes the per-thread temporary-file directory, falling back to the process-wide default.
	 */
	public static void clearTempDirectory() {
		TEMP_DIRECTORY.remove();
	}

	/**
	 * @return the directory that will be used for new temporary files on the current thread: the
	 * per-thread override if set, otherwise the process-wide default, otherwise {@code null} (JVM default)
	 */
	public static File getTempDirectory() {
		File perThread = TEMP_DIRECTORY.get();
		return perThread != null ? perThread : defaultTempDirectory;
	}

	/**
	 * Creates a temporary file, honouring the configured directory. Equivalent to
	 * {@link File#createTempFile(String, String)} when no directory is configured.
	 *
	 * @param prefix file-name prefix, as for {@link File#createTempFile(String, String, File)}
	 * @param suffix file-name suffix, or {@code null}
	 * @return the newly created temporary file
	 * @throws IOException if the file could not be created
	 */
	public static File createTempFile(String prefix, String suffix) throws IOException {
		File directory = getTempDirectory();
		return directory != null
				? File.createTempFile(prefix, suffix, directory)
				: File.createTempFile(prefix, suffix);
	}
}
