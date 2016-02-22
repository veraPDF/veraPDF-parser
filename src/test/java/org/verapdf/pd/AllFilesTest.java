package org.verapdf.pd;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class AllFilesTest {

	public static final String TEST_SUITE_DIR = "/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/test_suite";
	public static final String SAVED_FILE = "/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/test_suite_saved.pdf";
	public static final String SAVED_TWICE_FILE = "/home/timur/Projects/preforma/origin/veraPDF-pdflib/src/test/resources/test_suite_saved_twice.pdf";

	@Test
	public void testAllFiles() {
		File folder = new File(TEST_SUITE_DIR);
		File[] files = folder.listFiles();
		int i = 0;
		List<File> errors = new ArrayList<File>();
		for (File file : files) {
			try {
				i++;
				System.out.print("Processing : " + file.getName());
				PDDocument document = new PDDocument(file.getAbsolutePath());
				document.saveAs(SAVED_FILE);
				document = new PDDocument(SAVED_FILE);
				document.saveAs(SAVED_TWICE_FILE);
				System.out.println(" SUCCESS");
			} catch (RuntimeException re) {
				if (re.getMessage() != null && re.getMessage().contains("encrypted")) {
					System.out.println(" ENCRYPTED");
				} else if (re.getMessage() != null && re.getMessage().contains("xref streams not supported")) {
					System.out.println(" XREF STREAM");
				} else if (re.getMessage() != null &&
				     	  		(re.getMessage().contains("PDFParser::GetXRefSection(...)can not locate xref table") ||
								 re.getMessage().contains("PDFParser::GetXRefInfo(...)startxref validation failed"))) {
					System.out.println(" XREF POSITION ERROR");
				} else {
					System.out.println(" FAIL");
					errors.add(file);
				}
			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().contains("encrypted")) {
					System.out.println(" ENCRYPTED");
				} else if (e.getMessage() != null && e.getMessage().contains("xref streams not supported")) {
					System.out.println(" XREF STREAM");
				} else if (e.getMessage() != null &&
								(e.getMessage().contains("PDFParser::GetXRefSection(...)can not locate xref table") ||
								 e.getMessage().contains("PDFParser::GetXRefInfo(...)startxref validation failed"))) {
					System.out.println(" XREF POSITION ERROR");
				} else {
					System.out.println(" FAIL");
					errors.add(file);
				}
			}
		}

		System.out.println("Errors total : " + errors.size());
		for (File file : errors) {
			System.out.println(file);
		}
	}

}
