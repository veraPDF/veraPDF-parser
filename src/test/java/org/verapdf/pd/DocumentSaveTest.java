package org.verapdf.pd;

import org.junit.Test;

/**
 * @author Timur Kamalov
 */
public class DocumentSaveTest {

	public static final String FILE_NAME = "002731.pdf";

	@Test
	public void test() throws Exception {
		PDDocument document = new PDDocument("/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/test_suite/" + FILE_NAME);
		document.saveAs("/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/SmallAndValid_saved.pdf");
		System.out.println("Document saved");
		document = new PDDocument("/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/SmallAndValid_saved.pdf");
		document.saveAs("/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/SmallAndValid_saved_twice.pdf");
		System.out.println("Document saved twice");
	}

}
