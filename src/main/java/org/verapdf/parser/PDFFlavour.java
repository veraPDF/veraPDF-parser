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
	WCAG2_1(Specification.WCAG2_1),
	WCAG2_2(Specification.WCAG2_2);

	private final Specification specification;

	private PDFFlavour(final Specification specification) {
		this.specification = specification;
	}

	public Specification getSpecification() {
		return specification;
	}

	public enum Specification {
		NO_STANDARD(SpecificationFamily.NONE),
		ISO_14289_1(SpecificationFamily.PDF_UA),
		ISO_14289_2(SpecificationFamily.PDF_UA),
		ISO_19005_1(SpecificationFamily.PDF_A),
		ISO_19005_2(SpecificationFamily.PDF_A),
		ISO_19005_3(SpecificationFamily.PDF_A),
		ISO_19005_4(SpecificationFamily.PDF_A),
		WCAG2_1(SpecificationFamily.WCAG),
		WCAG2_2(SpecificationFamily.WCAG);

		private final SpecificationFamily family;

		Specification(final SpecificationFamily family) {
			this.family = family;
		}
		
		public SpecificationFamily getFamily() {
			return family;
		}
	}
	
	public enum SpecificationFamily {
		NONE,
		PDF_A,
		PDF_UA,
		WCAG
	}
}
