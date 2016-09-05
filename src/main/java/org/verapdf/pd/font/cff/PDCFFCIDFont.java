package org.verapdf.pd.font.cff;

import org.verapdf.cos.COSDictionary;
import org.verapdf.pd.font.PDFont;

/**
 * @author Sergey Shemyakov
 */
public class PDCFFCIDFont extends PDFont {

    public PDCFFCIDFont(COSDictionary dictionary) {
        super(dictionary);
    }
}
