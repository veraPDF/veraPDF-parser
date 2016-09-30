package org.verapdf.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents W array in CID fonts.
 *
 * @author Sergey Shemyakov
 */
public class CIDWArray {

    private static final Logger LOGGER = Logger.getLogger(CIDWArray.class);

    private Map<Integer, Double> singleMappings;
    private List<CIDWArrayRange> ranges;

    public CIDWArray(COSArray w) {
        singleMappings = new HashMap<>();
        ranges = new ArrayList<>();
        if(w != null) {
            for(int i = 0; i < w.size(); ++i) {
                int cidBegin = w.at(i++).getInteger().intValue();
                COSObject obj = w.at(i);
                if(obj.getType() == COSObjType.COS_INTEGER) {
                    int cidEnd = obj.getInteger().intValue();
                    Double width = w.at(++i).getReal();
                    if(width == null) {
                        LOGGER.debug("Unexpected end of W array in CID font");
                        return;
                    }
                    this.ranges.add(new CIDWArrayRange(cidBegin, cidEnd, width));
                } else if (obj.getType() == COSObjType.COS_ARRAY) {
                    addSingleMappings(cidBegin, (COSArray) obj.getDirectBase());
                }
            }
        }
    }

    private void addSingleMappings(int cidBegin, COSArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            if(arr.at(i).getType().isNumber()) {
                LOGGER.debug("W array in CIDFont has invalid entry.");
                continue;
            }
            this.singleMappings.put(cidBegin + i, arr.at(i).getReal());
        }
    }

    public Double getWidth(int cid) {
        Double res = singleMappings.get(cid);
        if(res == null) {
            for(CIDWArrayRange range: ranges) {
                if(range.contains(cid)) {
                    res = range.getWidth();
                    break;
                }
            }
        }
        return res;
    }

}
