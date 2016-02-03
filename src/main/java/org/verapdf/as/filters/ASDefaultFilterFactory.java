package org.verapdf.as.filters;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;

/**
 * @author Timur Kamalov
 */
public class ASDefaultFilterFactory implements IASFilterFactory {

	public ASInFilter getInFilter(final ASInputStream inputStream) {
		return new ASInFilter(inputStream);
	}

	public ASOutFilter getOutFilter(ASOutputStream outputStream) {
		return new ASOutFilter(outputStream);
	}
}
