package org.verapdf.pd.font.stdmetrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.verapdf.as.io.ASFileInStream;
import org.verapdf.as.io.ASInputStream;

/**
 * @author Sergey Shemyakov
 */
public class StandardFontMetricsFactory {

    private static final Logger LOGGER = Logger.getLogger(StandardFontMetricsFactory.class.getCanonicalName());
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
                LOGGER.log(Level.FINE, "Can't open file input stream for predefined font file " + DIR_PATH + fontName + EXTENSION, e);
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
                input.close();
                out.close();
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
            LOGGER.log(Level.FINE, "Error in opening predefined font metrics file " + fileName, e);
            return null;
        }
    }
}
