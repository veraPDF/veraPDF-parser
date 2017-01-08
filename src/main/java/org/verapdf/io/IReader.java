package org.verapdf.io;

import org.verapdf.cos.COSHeader;
import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSTrailer;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public interface IReader extends Closeable {

	SeekableInputStream getPDFSource();

	COSHeader getHeader();

	List<COSKey> getKeys();

	COSObject getObject(final COSKey key) throws IOException;

	COSObject getObject(final long offset) throws IOException;

	Long getOffset(final COSKey key);

	long getStartXRef();

	boolean isLinearized();

	COSTrailer getTrailer();

	COSTrailer getFirstTrailer();

	COSTrailer getLastTrailer();

	long getLastTrailerOffset();

	int getGreatestKeyNumberFromXref();
}
