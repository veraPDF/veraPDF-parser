package org.verapdf.pd.structure;

import org.verapdf.cos.COSObject;

import java.util.Iterator;

public class NumberTreeIterator implements Iterator<COSObject> {

	private NumberTreeIterator innerCurrentIterator;
	private final Iterator<COSObject> numbersIterator;
	private final Iterator<PDNumberTreeNode> kidsIterator;

	public NumberTreeIterator(PDNumberTreeNode root) {
		numbersIterator = root.getNums().values().iterator();
		kidsIterator = root.getKids().iterator();
		nextInnerIterator();
	}

	@Override
	public boolean hasNext() {
		return (innerCurrentIterator != null && innerCurrentIterator.hasNext()) || kidsIterator.hasNext() || numbersIterator.hasNext();
	}

	@Override
	public COSObject next() {
		if (numbersIterator.hasNext()) {
			return numbersIterator.next();
		}
		if (!innerCurrentIterator.hasNext()) {
			nextInnerIterator();
		}
		return innerCurrentIterator.next();
	}

	private void nextInnerIterator() {
		innerCurrentIterator = kidsIterator.hasNext() ? new NumberTreeIterator(kidsIterator.next()) : null;
	}
}
