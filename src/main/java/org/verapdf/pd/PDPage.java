/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.*;
import org.verapdf.pd.actions.PDPageAdditionalActions;
import org.verapdf.pd.colors.PDColorSpace;

import java.io.IOException;
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

        super.setObject(obj);

        initializeContents(obj);
    }

    private void initializeContents(final COSObject pageDict) {
        COSObject contents = pageDict.getKey(ASAtom.CONTENTS);
        if (!contents.empty() && contents.getType() == COSObjType.COS_STREAM) {
            this.content = new PDPageContentStream(contents);
        } else if (!contents.empty() && contents.getType() == COSObjType.COS_ARRAY) {
            try {
                this.content = new PDPageContentStream(COSStream.concatenateStreams((COSArray) contents.getDirectBase()));
            } catch (IOException e) {
                this.content = new PDPageContentStream(contents.at(0));
            }
        }
    }

    public double[] getMediaBox() {
        COSArray array = getInheritedCOSBBox(ASAtom.MEDIA_BOX);
        if (array != null) {
            return getDoubleArrayForBox(array);
        }
        return null;
    }

    public double[] getCropBox() {
        COSArray array = getInheritedCOSBBox(ASAtom.CROP_BOX);
        if (array != null) {
            return clipToMediaBox(getDoubleArrayForBox(array));
        } else {
            return getMediaBox();
        }
    }

    public double[] getBleedBox() {
        COSArray array = getCOSBBox(ASAtom.BLEED_BOX);
        if (array != null) {
            return clipToMediaBox(getDoubleArrayForBox(array));
        } else {
            return getCropBox();
        }
    }

    public double[] getTrimBox() {
        COSArray array = getCOSBBox(ASAtom.TRIM_BOX);
        if (array != null) {
            return clipToMediaBox(getDoubleArrayForBox(array));
        } else {
            return getCropBox();
        }
    }

    public double[] getArtBox() {
        COSArray array = getCOSBBox(ASAtom.ART_BOX);
        if (array != null) {
            return clipToMediaBox(getDoubleArrayForBox(array));
        } else {
            return getCropBox();
        }
    }

    private double[] clipToMediaBox(double[] box) {
        double[] res = new double[4];
        double[] mediaBox = getMediaBox();
        res[0] = Math.max(box[0], mediaBox[0]);
        res[1] = Math.max(box[1], mediaBox[1]);
        res[2] = Math.min(box[2], mediaBox[2]);
        res[3] = Math.min(box[3], mediaBox[3]);
        return res;
    }

    private static double[] getDoubleArrayForBox(COSArray array) {
        if (array == null) {
            return null;
        }
        double[] res = new double[4];
        for (int i = 0; i < array.size(); ++i) {
            COSObject obj = array.at(i);
            if (obj.getType().isNumber()) {
                res[i] = obj.getReal().doubleValue();
            } else {
                res[i] = 0;
            }
        }
        return res;
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
        if (!annots.empty() && annots.getType() == COSObjType.COS_ARRAY) {
            List<PDAnnotation> res = new ArrayList<>();
            if (annots.isIndirect()) {
                annots = annots.getDirect();
            }
            for (COSObject annot : (COSArray) annots.getDirectBase()) {
                if (annot != null && annot.getType() == COSObjType.COS_DICT) {
                    res.add(new PDAnnotation(annot));
                }
            }
            return Collections.unmodifiableList(res);
        } else if (annots.empty()) {
            annots = COSArray.construct();
            this.setKey(ASAtom.ANNOTS, annots);
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
        return pageNumber;
    }

    public Long getRotation() {
        COSObject current = getObject();
        while (current != null && current.getType().isDictionaryBased()) {
            COSObject object = current.getKey(ASAtom.ROTATE);
            if (object != null && !object.empty()) {
                if (object.getType() == COSObjType.COS_INTEGER) {
                    return object.getInteger();
                } else {
                    return Long.valueOf(0);
                }
            } else {
                current = current.getKey(ASAtom.PARENT);
            }
        }
        return Long.valueOf(0);
    }

    public Double getScaling() {
        return getObject().getRealKey(ASAtom.PZ);
    }

    public PDMetadata getMetadata() {
        COSObject obj = getKey(ASAtom.METADATA);
        if (obj.getType() == COSObjType.COS_STREAM) {
            return new PDMetadata(obj);
        }
        return null;
    }

    public PDNavigationNode getPresSteps() {
        COSObject cosPresSteps = getCOSPresSteps();
        if (cosPresSteps != null) {
            return new PDNavigationNode(cosPresSteps);
        }
        return null;
    }
}
