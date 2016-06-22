package org.verapdf.as.filters;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;

import java.io.IOException;

/**
 * @author Timur Kamalov
 */
public interface IASFilterFactory {

	ASInFilter getInFilter(ASInputStream inputStream) throws IOException;	//TODO: add decodeParams here

	ASOutFilter getOutFilter(ASOutputStream outputStream) throws IOException;	//TODO: add decodeParams here

}
