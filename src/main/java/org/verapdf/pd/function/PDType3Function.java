package org.verapdf.pd.function;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PDType3Function extends PDFunction {

	private static final Logger LOGGER = Logger.getLogger(PDType3Function.class.getCanonicalName());

	protected PDType3Function(COSObject obj) {
		super(obj);
	}

	public List<PDFunction> getFunctions() {
		COSObject obj = getKey(ASAtom.FUNCTIONS);
		if (obj.getType() != COSObjType.COS_ARRAY) {
			LOGGER.log(Level.WARNING, "Invalid Functions key value in Type 3 Function dictionary");
			return Collections.emptyList();
		}

		List<PDFunction> pdFunctions = new ArrayList<>();
		for (int i = 0; i < obj.size(); i++) {
			PDFunction function = PDFunction.createFunction(obj.at(i));
			if (function != null) {
				pdFunctions.add(function);
			}
		}
		return Collections.unmodifiableList(pdFunctions);
	}
}
