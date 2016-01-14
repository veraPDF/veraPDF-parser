package org.verapdf.pd;

import org.junit.Test;

/**
 * @author Timur Kamalov
 */
public class PageCountTest {


	@Test
	public void testPageCount() throws Exception {
		PDDocument document = new PDDocument("/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/SmallAndValid.pdf");
		System.out.println(document.getNumberOfPages());
	}

}
