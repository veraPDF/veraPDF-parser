package org.verapdf.cos.filters;

import org.junit.Test;
import org.verapdf.io.InternalInputStream;
import org.verapdf.io.InternalOutputStream;
import org.verapdf.pd.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Sergey Shemyakov
 */
public class COSFilterFlateEncodeTest {

    private static final String FILE_PATH =
            "src/test/resources/org/verapdf/cos/filters/validDocument.pdf";

    @Test
    public void test() throws IOException {

        byte[] toEncode = getDataToEncode();
        File encodedPDF = File.createTempFile("tmp_pdf_file", ".pdf");
        encodedPDF.deleteOnExit();
        encodePDF(toEncode, encodedPDF);
        InternalInputStream inputStream = new InternalInputStream(encodedPDF.getAbsolutePath());
        COSFilterFlateDecode decoder = new COSFilterFlateDecode(inputStream);
        PDDocument doc = new PDDocument(decoder);
        decoder.close();
    }

    private byte[] getDataToEncode() throws IOException {
        byte[] file = new byte[20000];
        InternalInputStream stream = new InternalInputStream(FILE_PATH);
        int length = stream.read(file, 20000);
        stream.close();
        return Arrays.copyOf(file, length);
    }

    private void encodePDF(byte[] toEncode, File encodedPDF) throws IOException {
        InternalOutputStream outputStream = new InternalOutputStream(encodedPDF.getAbsolutePath());
        COSFilterFlateEncode filter = new COSFilterFlateEncode(outputStream);
        filter.write(toEncode);
        filter.close();
    }
}
