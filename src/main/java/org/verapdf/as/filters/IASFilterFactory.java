package org.verapdf.as.filters;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;

/**
 * @author Timur Kamalov
 */
public interface IASFilterFactory {

	ASInFilter getInFilter(ASInputStream inputStream);

	ASOutFilter getOutFilter(ASOutputStream outputStream);

}
