package org.verapdf.pd.font.cmap;

import org.verapdf.parser.PDFFlavour;
import org.verapdf.tools.StaticResources;

import java.util.HashMap;
import java.util.Map;

public class CharacterCollections {

	private static final Map<String, CIDSystemInfo[]> map = new HashMap<>();

	static {
		map.put("GB-EUC-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_0, CIDSystemInfo.ADOBE_GB1_0,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("GB-EUC-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_0, CIDSystemInfo.ADOBE_GB1_0,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("GBpc-EUC-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_0, CIDSystemInfo.ADOBE_GB1_0,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("GBpc-EUC-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_0, CIDSystemInfo.ADOBE_GB1_0,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("GBK-EUC-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_2, CIDSystemInfo.ADOBE_GB1_2,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("GBK-EUC-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_2, CIDSystemInfo.ADOBE_GB1_2,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("GBKp-EUC-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_2, CIDSystemInfo.ADOBE_GB1_2,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("GBKp-EUC-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_2, CIDSystemInfo.ADOBE_GB1_2,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("GBK2K-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_4, CIDSystemInfo.ADOBE_GB1_4,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("GBK2K-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_4, CIDSystemInfo.ADOBE_GB1_4,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("UniGB-UCS2-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_4, CIDSystemInfo.ADOBE_GB1_4,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("UniGB-UCS2-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_GB1_4, CIDSystemInfo.ADOBE_GB1_4,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("UniGB-UTF16-H", new CIDSystemInfo[]{null, CIDSystemInfo.ADOBE_GB1_4,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("UniGB-UTF16-V", new CIDSystemInfo[]{null, CIDSystemInfo.ADOBE_GB1_4,
				CIDSystemInfo.ADOBE_GB1_5});
		map.put("B5pc-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_0, CIDSystemInfo.ADOBE_CNS1_0,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("B5pc-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_0, CIDSystemInfo.ADOBE_CNS1_0,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("HKscs-B5-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_3, CIDSystemInfo.ADOBE_CNS1_3,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("HKscs-B5-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_3, CIDSystemInfo.ADOBE_CNS1_3,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("ETen-B5-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_0, CIDSystemInfo.ADOBE_CNS1_0,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("ETen-B5-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_0, CIDSystemInfo.ADOBE_CNS1_0,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("ETenms-B5-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_0, CIDSystemInfo.ADOBE_CNS1_0,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("ETenms-B5-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_0, CIDSystemInfo.ADOBE_CNS1_0,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("CNS-EUC-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_0, CIDSystemInfo.ADOBE_CNS1_0,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("CNS-EUC-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_0, CIDSystemInfo.ADOBE_CNS1_0,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("UniCNS-UCS2-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_3, CIDSystemInfo.ADOBE_CNS1_3,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("UniCNS-UCS2-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_CNS1_3, CIDSystemInfo.ADOBE_CNS1_3,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("UniCNS-UTF16-H", new CIDSystemInfo[]{null, CIDSystemInfo.ADOBE_CNS1_4,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("UniCNS-UTF16-V", new CIDSystemInfo[]{null, CIDSystemInfo.ADOBE_CNS1_4,
				CIDSystemInfo.ADOBE_CNS1_7});
		map.put("83pv-RKSJ-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_1, CIDSystemInfo.ADOBE_JAPAN1_1,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("90ms-RKSJ-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_2, CIDSystemInfo.ADOBE_JAPAN1_2,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("90ms-RKSJ-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_2, CIDSystemInfo.ADOBE_JAPAN1_2,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("90msp-RKSJ-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_2, CIDSystemInfo.ADOBE_JAPAN1_2,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("90msp-RKSJ-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_2, CIDSystemInfo.ADOBE_JAPAN1_2,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("90pv-RKSJ-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_1, CIDSystemInfo.ADOBE_JAPAN1_1,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("Add-RKSJ-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_1, CIDSystemInfo.ADOBE_JAPAN1_1,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("Add-RKSJ-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_1, CIDSystemInfo.ADOBE_JAPAN1_1,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("EUC-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_1, CIDSystemInfo.ADOBE_JAPAN1_1,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("EUC-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_1, CIDSystemInfo.ADOBE_JAPAN1_1,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("Ext-RKSJ-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_2, CIDSystemInfo.ADOBE_JAPAN1_2,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("Ext-RKSJ-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_2, CIDSystemInfo.ADOBE_JAPAN1_2,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_1, CIDSystemInfo.ADOBE_JAPAN1_1,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_1, CIDSystemInfo.ADOBE_JAPAN1_1,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("UniJIS-UCS2-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_4, CIDSystemInfo.ADOBE_JAPAN1_4,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("UniJIS-UCS2-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_4, CIDSystemInfo.ADOBE_JAPAN1_4,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("UniJIS-UCS2-HW-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_4, CIDSystemInfo.ADOBE_JAPAN1_4,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("UniJIS-UCS2-HW-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_JAPAN1_4, CIDSystemInfo.ADOBE_JAPAN1_4,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("UniJIS-UTF16-H", new CIDSystemInfo[]{null, CIDSystemInfo.ADOBE_JAPAN1_5,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("UniJIS-UTF16-V", new CIDSystemInfo[]{null, CIDSystemInfo.ADOBE_JAPAN1_5,
				CIDSystemInfo.ADOBE_JAPAN1_7});
		map.put("KSC-EUC-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_KOREA1_0, CIDSystemInfo.ADOBE_KOREA1_0,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("KSC-EUC-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_KOREA1_0, CIDSystemInfo.ADOBE_KOREA1_0,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("KSCms-UHC-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_KOREA1_1, CIDSystemInfo.ADOBE_KOREA1_1,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("KSCms-UHC-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_KOREA1_1, CIDSystemInfo.ADOBE_KOREA1_1,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("KSCms-UHC-HW-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_KOREA1_1, CIDSystemInfo.ADOBE_KOREA1_1,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("KSCms-UHC-HW-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_KOREA1_1, CIDSystemInfo.ADOBE_KOREA1_1,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("KSCpc-EUC-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_KOREA1_0, CIDSystemInfo.ADOBE_KOREA1_0,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("UniKS-UCS2-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_KOREA1_1, CIDSystemInfo.ADOBE_KOREA1_1,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("UniKS-UCS2-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_KOREA1_1, CIDSystemInfo.ADOBE_KOREA1_1,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("UniKS-UTF16-H", new CIDSystemInfo[]{null, CIDSystemInfo.ADOBE_KOREA1_2,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("UniKS-UTF16-V", new CIDSystemInfo[]{null, CIDSystemInfo.ADOBE_KOREA1_2,
				CIDSystemInfo.ADOBE_KR_9});
		map.put("Identity-H", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_IDENTITY_0, CIDSystemInfo.ADOBE_IDENTITY_0,
				CIDSystemInfo.ADOBE_IDENTITY_0});
		map.put("Identity-V", new CIDSystemInfo[]{CIDSystemInfo.ADOBE_IDENTITY_0, CIDSystemInfo.ADOBE_IDENTITY_0,
				CIDSystemInfo.ADOBE_IDENTITY_0});
	}

	public static CIDSystemInfo getCIDSystemInfo(String cmapName) {
		CIDSystemInfo[] cidSystemInfos = map.get(cmapName);
		if (cidSystemInfos != null) {
			PDFFlavour flavour = StaticResources.getFlavour();
			if (PDFFlavour.isFlavourPDFSpecification(flavour, PDFFlavour.PDFSpecification.PDF_REFERENCE_1_4)) {
				return cidSystemInfos[0];
			} else if (PDFFlavour.isFlavourPDFSpecification(flavour, PDFFlavour.PDFSpecification.ISO_32000_2_0)) {
				return cidSystemInfos[2];
			} else {
				return cidSystemInfos[1];
			}
		}
		return null;
	}

	public enum CIDSystemInfo {
		ADOBE_GB1_0(Registry.ADOBE, Ordering.GB1, 0),
		ADOBE_GB1_2(Registry.ADOBE, Ordering.GB1, 2),
		ADOBE_GB1_4(Registry.ADOBE, Ordering.GB1, 4),
		ADOBE_GB1_5(Registry.ADOBE, Ordering.GB1, 5),
		ADOBE_CNS1_0(Registry.ADOBE, Ordering.CNS1, 0),
		ADOBE_CNS1_3(Registry.ADOBE, Ordering.CNS1, 3),
		ADOBE_CNS1_4(Registry.ADOBE, Ordering.CNS1, 4),
		ADOBE_CNS1_7(Registry.ADOBE, Ordering.CNS1, 7),
		ADOBE_JAPAN1_1(Registry.ADOBE, Ordering.JAPAN1, 1),
		ADOBE_JAPAN1_2(Registry.ADOBE, Ordering.JAPAN1, 2),
		ADOBE_JAPAN1_4(Registry.ADOBE, Ordering.JAPAN1, 4),
		ADOBE_JAPAN1_5(Registry.ADOBE, Ordering.JAPAN1, 5),
		ADOBE_JAPAN1_7(Registry.ADOBE, Ordering.JAPAN1, 7),
		ADOBE_KOREA1_0(Registry.ADOBE, Ordering.KOREA1, 0),
		ADOBE_KOREA1_1(Registry.ADOBE, Ordering.KOREA1, 1),
		ADOBE_KOREA1_2(Registry.ADOBE, Ordering.KOREA1, 2),
		ADOBE_KR_9(Registry.ADOBE, Ordering.KR, 9),
		ADOBE_IDENTITY_0(Registry.ADOBE, Ordering.IDENTITY, 0);

		private final Registry registry;
		private final Ordering ordering;
		private final int supplement;

		CIDSystemInfo(Registry registry, Ordering ordering, int supplement) {
			this.registry = registry;
			this.ordering = ordering;
			this.supplement = supplement;
		}

		public Registry getRegistry() {
			return registry;
		}

		public Ordering getOrdering() {
			return ordering;
		}

		public int getSupplement() {
			return supplement;
		}

		public enum Registry {
			ADOBE("Adobe");

			private final String registry;

			Registry(String registry) {
				this.registry = registry;
			}

			public String getRegistry() {
				return registry;
			}
		}

		public enum Ordering {
			GB1("GB1"),
			CNS1("CNS1"),
			JAPAN1("Japan1"),
			KOREA1("Korea1"),
			IDENTITY("Identity"),
			KR("KR");

			private final String ordering;

			Ordering(String ordering) {
				this.ordering = ordering;
			}

			public String getOrdering() {
				return ordering;
			}
		}
	}
}
