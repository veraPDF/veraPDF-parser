package org.verapdf.parser;

public enum PDFFlavour {
	NO_FLAVOUR(Specification.NO_STANDARD),
	PDFA_1_A(Specification.ISO_19005_1),
	PDFA_1_B(Specification.ISO_19005_1),
	PDFA_2_A(Specification.ISO_19005_2),
	PDFA_2_B(Specification.ISO_19005_2),
	PDFA_2_U(Specification.ISO_19005_2),
	PDFA_3_A(Specification.ISO_19005_3),
	PDFA_3_B(Specification.ISO_19005_3),
	PDFA_3_U(Specification.ISO_19005_3),
	PDFA_4(Specification.ISO_19005_4),
	PDFA_4_F(Specification.ISO_19005_4),
	PDFA_4_E(Specification.ISO_19005_4),
	PDFUA_1(Specification.ISO_14289_1),
	PDFUA_2(Specification.ISO_14289_2),
	WCAG2_1(Specification.WCAG_2_1);

	private final Specification specification;
	
	private PDFFlavour(final Specification specification) {
		this.specification = specification;
	}

	public Specification getSpecification() {
		return specification;
	}

	public enum Specification {
		NO_STANDARD,
		ISO_14289_1,
		ISO_14289_2,
		ISO_19005_1,
		ISO_19005_2,
		ISO_19005_3,
		ISO_19005_4,
		WCAG_2_1
	}
}
