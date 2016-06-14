package org.verapdf.operator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class Operator {

	private static final Map<String, Operator> cachedOperators = new HashMap<>();

	private String operator;

	private Operator(final String operator) {
		this.operator = operator;
	}

	public static Operator getOperator(final String operator) {
		//don't cache image operators due to unique parameters and data
		if (operator.equals("BI") || operator.equals("ID")) {
			return new Operator(operator);
		} else {
			if (cachedOperators.containsKey(operator)) {
				return cachedOperators.get(operator);
			} else {
				Operator result = new Operator(operator);
				cachedOperators.put(operator, result);
				return result;
			}
		}
	}

	public String getOperator() {
		return operator;
	}

}
