package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSIndirect;
import org.verapdf.cos.COSObject;

import java.io.IOException;

/**
 * Created by Timur on 1/10/2016.
 */
public class PDPage extends PDPageTreeNode {

    //! Predefined page sizes
    public static double PAGE_SIZE_A3[] = {0, 0, 842, 1190};
    public static double PAGE_SIZE_A4[] = {0, 0, 595, 842};
    public static double PAGE_SIZE_A5[] = {0, 0, 420, 595};
    public static double PAGE_SIZE_B4[] = {0, 0, 709, 1001};
    public static double PAGE_SIZE_B5[] = {0, 0, 499, 709};
    public static double PAGE_SIZE_LETTER[] = {0, 0, 612, 1008};
    public static double PAGE_SIZE_LEGAL[] = {0, 0, 612, 792};

    //private PDContentStream content;
    int num;
    int totalNum;

    public PDPage(final double bbox[], final COSDocument doc) throws Exception {
        super();
        this.num = 0;
        this.totalNum = 0;
        COSObject page = COSDictionary.construct();
        page.setNameKey(ASAtom.TYPE, ASAtom.PAGE);
        page.setArrayKey(ASAtom.MEDIA_BOX, 4, bbox);
        page.setArrayKey(ASAtom.CONTENTS);
        COSObject obj = COSIndirect.construct(page, doc);
        setObject(obj);
    }

    public PDPage(final COSObject obj) throws Exception {
        super();
        this.num = 0;
        this.totalNum = 0;
        setObject(obj);
    }

    public void setBBox(final double[] bbox, final ASAtom boxType) throws IOException {
        getObject().setArrayKey(boxType, 4, bbox);
    }

    public boolean getBBox(final double[] bbox, final ASAtom boxType) throws IOException {
        COSObject obj = getKey(boxType);
        if (!obj.empty() && obj.size() >= 4) {
            for (int i = 0; i < 4; i++) {
                bbox[i] = obj.at(i).getReal();
            }
            return true;
        } else {
            if (boxType == ASAtom.MEDIA_BOX)
            {
                // if we are here this means that page media box is missing. Return then the default one
                for (int i = 0; i < 4; i++)
                    bbox[i] = PDPage.PAGE_SIZE_LETTER[i];
            } else if (boxType == ASAtom.CROP_BOX) {
                getBBox(bbox, ASAtom.MEDIA_BOX);
            } else if (boxType == ASAtom.BLEED_BOX || boxType == ASAtom.TRIM_BOX || boxType == ASAtom.ART_BOX) {
                getBBox(bbox, ASAtom.CROP_BOX);
            } else {
                getBBox(bbox, ASAtom.MEDIA_BOX);
            }

            return false;
        }
    }

    public PDDocument getPDDoc() {
        COSDocument cosDoc = getObject().getDocument();
        if (cosDoc != null) {
            return cosDoc.getPDDoc();
        } else {
            return null;
        }
    }

    //TODO : implement this
    /*
    public PDResources getResources() {
        COSObject resDict = getKey(ASAtom.RESOURCES);
        COSDocument doc = getObject().getDocument();
        if (resDict.Empty())
        {
            resDict = COSDictionary.construct();
            COSObject ind = COSIndirect.construct(resDict, doc);
            setKey(ASAtom.RESOURCES, ind);
            return PDResources(ind, doc);
        }
        else
            return PDResources(resDict, doc);
    }
    */

    public int getPageNumber() {
        return this.totalNum;
    }

    //TODO : impelement this
    /*
    public String getLabel() {
        return this.getPDDoc().getCatalog().getPageLabels().getLabel(getPageNumber());
    }
    */
}
