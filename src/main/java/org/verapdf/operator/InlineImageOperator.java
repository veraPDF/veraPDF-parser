package org.verapdf.operator;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSDictionary;

/**
 * @author Timur Kamalov
 */
public class InlineImageOperator extends Operator {

	private ASInputStream imageData;
	private COSDictionary imageParameters;

	public InlineImageOperator(final String operator) {
		super(operator);
	}

	public ASInputStream getImageData() {
		return imageData;
	}

	public void setImageData(ASInputStream imageData) {
		this.imageData = imageData;
	}

	public COSDictionary getImageParameters() {
		return imageParameters;
	}

	public void setImageParameters(COSDictionary imageParameters) {
		this.imageParameters = imageParameters;
	}

}
