package org.verapdf.io;

import org.verapdf.cos.COSKey;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSTrailer;

import java.io.IOException;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public interface IReader {

	String getHeader();

	List<COSKey> getKeys();

	COSObject getObject(final COSKey key) throws IOException;

	COSObject getObject(final long offset) throws IOException;

	long getStartXRef();

	COSTrailer getTrailer();

	COSTrailer getFirstTrailer();

	COSTrailer getLastTrailer();

}
