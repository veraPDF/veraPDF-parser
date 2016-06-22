package org.verapdf.as.filters;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.COSDictionary;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public interface IASFilterFactory {

	ASInFilter getInFilter(ASInputStream inputStream, COSDictionary decodeParams) throws IOException;

	ASOutFilter getOutFilter(ASOutputStream outputStream) throws IOException;

}
