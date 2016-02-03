package org.verapdf.pd;

import org.junit.Test;

/**
 * @author Timur Kamalov
 */
public class DocumentSaveTest {

	@Test
	public void test() throws Exception {
		PDDocument document = new PDDocument("/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/SmallAndValid.pdf");
		document.saveAs("/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/SmallAndValid_saved.pdf");
	}

}
