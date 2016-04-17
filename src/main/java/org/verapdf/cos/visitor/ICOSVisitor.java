package org.verapdf.cos.visitor;

import org.verapdf.cos.*;

/**
 * Created by Timur on 4/16/2016.
 */
public interface ICOSVisitor {

    Object visitFromBoolean(final COSBoolean obj);
    Object visitFromInteger(final COSInteger obj);
    Object visitFromReal(final COSReal obj);
    Object visitFromString(final COSString obj);
    Object visitFromName(final COSName obj);
    Object visitFromArray(final COSArray obj);
    Object visitFromDictionary(final COSDictionary obj);
    Object visitFromDocument(final COSDocument obj);
    Object visitFromStream(final COSStream obj);
    Object visitFromNull(final COSNull obj);

}
