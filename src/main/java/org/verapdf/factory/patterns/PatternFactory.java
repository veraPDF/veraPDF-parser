package org.verapdf.factory.patterns;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;
import org.verapdf.pd.patterns.PDPattern;
import org.verapdf.pd.patterns.PDShadingPattern;
import org.verapdf.pd.patterns.PDTilingPattern;

/**
 * @author Maksim Bezrukov
 */
public class PatternFactory {

	private static final Logger LOGGER = Logger.getLogger(PatternFactory.class);

	private PatternFactory() {
	}

	public static PDPattern getPattern(COSObject base) {
		if (base == null) {
			return null;
		}

		Long patternType = base.getIntegerKey(ASAtom.PATTERN_TYPE);
		if (patternType != null) {
			int simplePatternType = patternType.intValue();
			if (simplePatternType == 1) {
				return new PDTilingPattern(base);
			} else if (simplePatternType == 2) {
				return new PDShadingPattern(base);
			} else {
				LOGGER.debug("PatternType value is not correct");
				return null;
			}
		} else {
			LOGGER.debug("COSObject doesn't a dictionary or doesn't contain PatternType key");
			return null;
		}
	}
}
