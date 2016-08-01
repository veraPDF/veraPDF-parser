package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSIndirect;
import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
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

    private PDResources resources;

    private PDContentStream content;

    int pageNumber;
    int pagesTotal;

    public PDPage(final double bbox[], final COSDocument document) {
        super();

        this.pageNumber = 0;
        this.pagesTotal = 0;

        final COSObject page = COSDictionary.construct();
        page.setNameKey(ASAtom.TYPE, ASAtom.PAGE);
        page.setArrayKey(ASAtom.MEDIA_BOX, 4, bbox);
        page.setArrayKey(ASAtom.CONTENTS);
        final COSObject indirect = COSIndirect.construct(page, document);
        this.setObject(indirect);
    }

    public PDPage(final COSObject obj) {
        super();
        this.pageNumber = 0;
        this.pagesTotal = 0;

        initializeContents(obj);

        super.setObject(obj);
    }

    private void initializeContents(final COSObject pageDict) {
        COSObject contents = pageDict.getKey(ASAtom.CONTENTS);
        if (!contents.empty()) {
            this.content = new PDPageContentStream(contents);
        }
    }

    public void setBBox(final double[] bbox, final ASAtom boxType) {
        this.getObject().setArrayKey(boxType, 4, bbox);
    }

    public boolean getBBox(final double[] bbox, final ASAtom boxType) {
        COSObject object = this.getKey(boxType);
        if (!object.empty() && object.size() >= 4) {
            for (int i = 0; i < 4; i++) {
                bbox[i] = object.at(i).getReal();
            }
            return true;
        } else {
            if (boxType == ASAtom.MEDIA_BOX) {
                // if we are here this means that page media box is missing. Return then the default one
                for (int i = 0; i < 4; i++) {
                    bbox[i] = PDPage.PAGE_SIZE_LETTER[i];
                }
            } else if (boxType == ASAtom.CROP_BOX) {
                this.getBBox(bbox, ASAtom.MEDIA_BOX);
            } else if (boxType == ASAtom.BLEED_BOX || boxType == ASAtom.TRIM_BOX || boxType == ASAtom.ART_BOX) {
                this.getBBox(bbox, ASAtom.CROP_BOX);
            } else {
                this.getBBox(bbox, ASAtom.MEDIA_BOX);
            }

            return false;
        }
    }

    public PDDocument getPDDocument() {
        final COSDocument cosDocument = super.getObject().getDocument();
        if (cosDocument != null) {
            return cosDocument.getPDDocument();
        } else {
            return null;
        }
    }

    public PDResources getResources() {
        if (this.resources != null) {
            return this.resources;
        } else {
            COSObject resources = getInheritableResources();
            if (resources != null) {
                this.resources = new PDResources(resources);
            }
        }

        return this.resources;
    }

    public Boolean isInheritedResources() {
        return getObject().knownKey(ASAtom.RESOURCES);
    }

    public void setResources(PDResources resources) {
        this.resources = resources;
        if (resources != null) {
            getObject().setKey(ASAtom.RESOURCES, resources.getObject());
        } else {
            getObject().removeKey(ASAtom.RESOURCES);
        }
    }

    public PDContentStream getContent() {
        return content;
    }

    public void setContent(PDContentStream content) {
        this.content = content;
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
        return pagesTotal;
    }

    //TODO : implement this
    /*
    public String getLabel() {
        return this.getPDDoc().getCatalog().getPageLabels().getLabel(getPageNumber());
    }
    */
}
