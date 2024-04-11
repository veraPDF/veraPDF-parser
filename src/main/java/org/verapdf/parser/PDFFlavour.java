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
	WTPDF_1_0_REUSE(Specification.WTPDF_1_0),
	WTPDF_1_0_ACCESSIBILITY(Specification.WTPDF_1_0),
	WCAG_2_1(Specification.WCAG_2_1),
	WCAG_2_2(Specification.WCAG_2_2);

	private final Specification specification;

	private PDFFlavour(final Specification specification) {
		this.specification = specification;
	}

	public Specification getSpecification() {
		return specification;
	}

	public enum Specification {
		NO_STANDARD(SpecificationFamily.NONE, PDFSpecification.NO_SPECIFICATION),
		ISO_14289_1(SpecificationFamily.PDF_UA, PDFSpecification.ISO_32000_1_7),
		ISO_14289_2(SpecificationFamily.PDF_UA, PDFSpecification.ISO_32000_2_0),
		ISO_19005_1(SpecificationFamily.PDF_A, PDFSpecification.PDF_REFERENCE_1_4),
		ISO_19005_2(SpecificationFamily.PDF_A, PDFSpecification.ISO_32000_1_7),
		ISO_19005_3(SpecificationFamily.PDF_A, PDFSpecification.ISO_32000_1_7),
		ISO_19005_4(SpecificationFamily.PDF_A, PDFSpecification.ISO_32000_2_0),
		WTPDF_1_0(SpecificationFamily.WTPDF, PDFSpecification.ISO_32000_2_0),
		WCAG_2_1(SpecificationFamily.WCAG, PDFSpecification.ISO_32000_2_0),
		WCAG_2_2(SpecificationFamily.WCAG, PDFSpecification.ISO_32000_2_0);

		private final SpecificationFamily family;
		private final PDFSpecification pdfSpecification;

		Specification(final SpecificationFamily family, final PDFSpecification pdfSpecification) {
			this.family = family;
			this.pdfSpecification = pdfSpecification;
		}
		
		public SpecificationFamily getFamily() {
			return family;
		}

		public PDFSpecification getPdfSpecification() {
			return pdfSpecification;
		}
	}
	
	public enum SpecificationFamily {
		NONE,
		PDF_A,
		PDF_UA,
		WCAG,
		WTPDF
	}

	public enum PDFSpecification {
		NO_SPECIFICATION,
		PDF_REFERENCE_1_4,
		ISO_32000_1_7,
		ISO_32000_2_0
	}
	
	public static boolean isFlavourPDFSpecification(PDFFlavour flavour, PDFSpecification pdfSpecification) {
		return flavour != null && flavour.getSpecification().getPdfSpecification() == pdfSpecification;
	}

	public static boolean isFlavourFamily(PDFFlavour flavour, SpecificationFamily specificationFamily) {
		return flavour != null && flavour.getSpecification().getFamily() == specificationFamily;
	}

	public static boolean isFlavour(PDFFlavour currentFlavour, PDFFlavour flavour) {
		return currentFlavour != null && currentFlavour == flavour;
	}

	public static boolean isFlavourPart(PDFFlavour flavour, Specification specificationPart) {
		return flavour != null && flavour.getSpecification() == specificationPart;
	}

	public static boolean isPDFUA2RelatedFlavour(PDFFlavour flavour) {
		return isFlavour(flavour, PDFFlavour.PDFUA_2) || isFlavourPart(flavour, Specification.WTPDF_1_0);
	}
} 
