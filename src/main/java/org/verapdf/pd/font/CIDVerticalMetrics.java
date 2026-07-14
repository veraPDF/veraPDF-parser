/*
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
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
package org.verapdf.pd.font;

/**
 * Represents vertical metrics in CID Fonts
 *
 * @author Vladimir Burshnev
 */
public class CIDVerticalMetrics {
    private final double displacementVectorH;
    private final double displacementVectorV;
    private final double positionVectorH;
    private final double positionVectorV;

    public CIDVerticalMetrics(double displacement, double positionVectorH, double positionVectorV) {
        this.displacementVectorV = displacement;
        this.displacementVectorH = 0;
        this.positionVectorH = positionVectorH;
        this.positionVectorV = positionVectorV;
    }

    public CIDVerticalMetrics(double displacement, double positionVector) {
        this(displacement, 0, positionVector);
    }

    /**
     * @return vertical component of the displacement vector
     */
    public double getDisplacement() {
        return displacementVectorV;
    }
}
