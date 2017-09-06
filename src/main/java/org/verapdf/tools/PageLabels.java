package org.verapdf.tools;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSArray;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Maksim Bezrukov
 */
public class PageLabels {

	private final TreeMap<Integer, PageLabelDictionary> labelsMap;

	public PageLabels(COSDictionary numbTree) {
		if (numbTree == null) {
			throw new IllegalArgumentException("Number tree base element can not be null");
		}
		labelsMap = new TreeMap<>();
		parseTree(numbTree);
	}

	private void parseTree(COSDictionary numbTree) {
		COSObject nums = numbTree.getKey(ASAtom.NUMS);
		if (nums != null && !nums.empty() && nums.getType() == COSObjType.COS_ARRAY) {
			addLabelsFromArray((COSArray) nums.getDirectBase());
		}

		COSObject kids = numbTree.getKey(ASAtom.KIDS);
		if (kids != null && !kids.empty() && kids.getType() == COSObjType.COS_ARRAY) {
			for (COSObject kid : (COSArray) kids.getDirectBase()) {
				if (kid != null && !kid.empty() && kid.getType() == COSObjType.COS_DICT) {
					parseTree((COSDictionary) kid.getDirectBase());
				}
			}
		}
	}

	private void addLabelsFromArray(COSArray nums) {
		for (int i = 0; i < nums.size(); i+=2) {
			COSObject cosKey = nums.at(i);
			Long key = cosKey == null ? null : cosKey.getInteger();
			COSObject cosValue = nums.at(i+1);
			if (key != null && cosValue != null && !cosValue.empty() && cosValue.getType() == COSObjType.COS_DICT) {
				PageLabelDictionary pageLabelDictionary = new PageLabelDictionary((COSDictionary) cosValue.getDirectBase(), key.intValue());
				this.labelsMap.put(key.intValue(), pageLabelDictionary);
			}
		}
	}

	public String getLabel(int pageIndex) {
		Map.Entry<Integer, PageLabelDictionary> pageLabelDictionaryEntry = labelsMap.floorEntry(pageIndex);
		if (pageLabelDictionaryEntry != null) {
			return pageLabelDictionaryEntry.getValue().getLabel(pageIndex);
		}
		return null;
	}

	private static class PageLabelDictionary {

		private final static TreeMap<Integer, String> MAP = new TreeMap<>();

		static {
			MAP.put(1000, "M");
			MAP.put(900, "CM");
			MAP.put(500, "D");
			MAP.put(400, "CD");
			MAP.put(100, "C");
			MAP.put(90, "XC");
			MAP.put(50, "L");
			MAP.put(40, "XL");
			MAP.put(10, "X");
			MAP.put(9, "IX");
			MAP.put(5, "V");
			MAP.put(4, "IV");
			MAP.put(1, "I");
		}

		private final ASAtom type;
		private final String prefix;
		private final int firstRangePortion;
		private final int rangeStartIndex;

		public PageLabelDictionary(COSDictionary dict, int rangeStartIndex) {
			this.type = dict.getNameKey(ASAtom.S);
			String prefixValue = dict.getStringKey(ASAtom.P);
			this.prefix = prefixValue == null ? "" : prefixValue;
			Long integerKey = dict.getIntegerKey(ASAtom.ST);
			this.firstRangePortion = integerKey == null ? 1 : integerKey.intValue();
			this.rangeStartIndex = rangeStartIndex;
		}

		public String getLabel(int pageIndex) {
			if (pageIndex < rangeStartIndex) {
				throw new IllegalArgumentException("Page index can not be less than range start index");
			}
			int pageNumber = getLabelNumber(pageIndex);
			String number = getTypedNumber(pageNumber);
			return this.prefix + number;
		}

		private String getTypedNumber(int pageNumber) {
			if (ASAtom.D == this.type) {
				return String.valueOf(pageNumber);
			} else if (ASAtom.R == this.type) {
				return getRoman(pageNumber);
			} else if (ASAtom.R_LOWERCASE == this.type) {
				return getRoman(pageNumber).toLowerCase();
			} else if (ASAtom.A == this.type) {
				return getLetters(pageNumber);
			} else if (ASAtom.A_LOWERCASE == this.type) {
				return getLetters(pageNumber).toLowerCase();
			} else {
				return "";
			}
		}

		private static String getLetters(int pageNumber) {
			int numb = pageNumber - 1;
			int remainder = numb % 26;
			char letter = (char) ('A' + remainder);
			int numberOfLetters = numb / 26 + 1;
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < numberOfLetters; ++i) {
				builder.append(letter);
			}
			return builder.toString();
		}

		private static String getRoman(int pageNumber) {
			int curr = pageNumber;
			StringBuilder builder = new StringBuilder();
			while (curr > 0) {
				int floor =  MAP.floorKey(curr);
				builder.append(MAP.get(floor));
				curr -= floor;
			}
			return builder.toString();
		}

		private int getLabelNumber(int pageIndex) {
			return pageIndex - rangeStartIndex + firstRangePortion;
		}
	}
}
