package org.verapdf.pd.font.stdmetrics;

import org.apache.log4j.Logger;
import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey Shemyakov
 */
public class StandardFontMetricsFactory {

    private static final Logger LOGGER = Logger.getLogger(StandardFontMetricsFactory.class);
    private static final String DIR_PATH = "/font/stdmetrics/";
    private static final String EXTENSION = ".afm";
    private static final Map<String, StandardFontMetrics> FONT_METRICS_MAP =
            new HashMap<>();

    private StandardFontMetricsFactory() {}

    public static StandardFontMetrics getFontMetrics(String fontName) {
        StandardFontMetrics res = FONT_METRICS_MAP.get(fontName);
        if (res == null) {
            try {
                String afmPath = DIR_PATH + fontName + EXTENSION;
                AFMParser parser = new AFMParser(load(afmPath), fontName);
                res = parser.parse();
                FONT_METRICS_MAP.put(fontName, res);
            } catch (IOException e) {
                LOGGER.error("Can't open file input stream for predefined font file " + DIR_PATH + fontName + EXTENSION);
                return null;
            }
        }
        return res;
    }

    private static ASInputStream load(String fileName) {
        try {
            File afmFile;
            URL res = StandardFontMetrics.class.getResource(fileName);
            if (res.toString().startsWith("jar:")) {
                InputStream input = StandardFontMetrics.class.getResourceAsStream(fileName);
                afmFile = File.createTempFile("tempfile", ".tmp");
                OutputStream out = new FileOutputStream(afmFile);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                afmFile.deleteOnExit();
            } else {
                afmFile = new File(res.getFile());
            }
            if (!afmFile.exists()) {
                throw new IOException("Error: File " + afmFile + " not found!");
            }
            return new ASFileInStream(
                    new RandomAccessFile(afmFile, "r"), 0, afmFile.length());
        } catch (IOException e) {
            LOGGER.debug("Error in opening predefined font metrics file " + fileName, e);
            return null;
        }
    }
}
