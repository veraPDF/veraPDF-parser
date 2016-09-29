package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.actions.PDPageAdditionalActions;
import org.verapdf.pd.colors.PDColorSpace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        if (!contents.empty() && contents.getType() == COSObjType.COS_STREAM) {
            this.content = new PDPageContentStream(contents);
        } else if (!contents.empty() && contents.getType() == COSObjType.COS_ARRAY) {
            //TODO : add content streams concatenation
            this.content = new PDPageContentStream(contents.at(0));
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
        return !getObject().knownKey(ASAtom.RESOURCES);
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

    public PDGroup getGroup() {
        COSObject group = getKey(ASAtom.GROUP);
        if (group != null && group.getType() == COSObjType.COS_DICT) {
            return new PDGroup(group);
        }
        return null;
    }

    public PDColorSpace getGroupCS() {
        PDGroup group = getGroup();
        if (group != null) {
            return group.getColorSpace();
        }
        return null;
    }

    public COSArray getCOSMediaBox() {
        return getInheritedCOSBBox(ASAtom.MEDIA_BOX);
    }

    public COSArray getCOSCropBox() {
        return getInheritedCOSBBox(ASAtom.CROP_BOX);
    }

    public COSArray getCOSBleedBox() {
        return getCOSBBox(ASAtom.BLEED_BOX);
    }

    public COSArray getCOSTrimBox() {
        return getCOSBBox(ASAtom.TRIM_BOX);
    }

    public COSArray getCOSArtBox() {
        return getCOSBBox(ASAtom.ART_BOX);
    }

    private COSArray getCOSBBox(ASAtom type) {
        COSObject object = getKey(type);
        if (object != null && object.getType() == COSObjType.COS_ARRAY) {
            return (COSArray) object.getDirectBase();
        }
        return null;
    }

    private COSArray getInheritedCOSBBox(ASAtom type) {
        COSObject current = getObject();
        while (current != null && current.getType().isDictionaryBased()) {
            COSObject object = current.getKey(type);
            if (object != null && !object.empty()) {
                if (object.getType() == COSObjType.COS_ARRAY) {
                    return (COSArray) object.getDirectBase();
                } else {
                    return null;
                }
            } else {
                current = current.getKey(ASAtom.PARENT);
            }
        }
        return null;
    }


    public COSObject getCOSPresSteps() {
        COSObject pres = getKey(ASAtom.PRES_STEPS);
        return (pres == null || pres.empty() || pres.getType() == COSObjType.COS_NULL) ? null : pres;
    }

    public List<PDAnnotation> getAnnotations() {
        COSObject annots = getKey(ASAtom.ANNOTS);
        if (annots != null && annots.getType() == COSObjType.COS_ARRAY) {
            List<PDAnnotation> res = new ArrayList<>();
            if (annots.isIndirect()) {
                annots = annots.getDirect();
            }
            for (COSObject annot : (COSArray) annots.get()) {
                if (annot != null && annot.getType() == COSObjType.COS_DICT) {
                    res.add(new PDAnnotation(annot));
                }
            }
            return Collections.unmodifiableList(res);
        }
        return Collections.emptyList();
    }

    public PDPageAdditionalActions getAdditionalActions() {
        COSObject aaDict = getKey(ASAtom.AA);
        if (aaDict != null && aaDict.getType() == COSObjType.COS_DICT) {
            return new PDPageAdditionalActions(aaDict);
        }
        return null;
    }

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
