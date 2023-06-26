package org.verapdf.pd.structure;

import org.verapdf.cos.COSObject;
import org.verapdf.pd.PDNameTreeNode;

import java.util.Iterator;

public class NameTreeIterator implements Iterator<COSObject> {

	private NameTreeIterator innerCurrentIterator;
	private final Iterator<COSObject> namesIterator;
	private final Iterator<PDNameTreeNode> kidsIterator;

	public NameTreeIterator(PDNameTreeNode root) {
		namesIterator = root.getNames().values().iterator();
		kidsIterator = root.getKids().iterator();
		nextInnerIterator();
	}

	@Override
	public boolean hasNext() {
		return (innerCurrentIterator != null && innerCurrentIterator.hasNext()) || kidsIterator.hasNext() || namesIterator.hasNext();
	}

	@Override
	public COSObject next() {
		if (namesIterator.hasNext()) {
			return namesIterator.next();
		}
		if (!innerCurrentIterator.hasNext()) {
			nextInnerIterator();
		}
		return innerCurrentIterator.next();
	}

	private void nextInnerIterator() {
		innerCurrentIterator = kidsIterator.hasNext() ? new NameTreeIterator(kidsIterator.next()) : null;
	}
}
