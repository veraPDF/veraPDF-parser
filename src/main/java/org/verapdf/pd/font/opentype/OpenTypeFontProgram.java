package org.verapdf.pd.font.opentype;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSObject;
import org.verapdf.io.SeekableInputStream;
import org.verapdf.pd.font.FontProgram;
import org.verapdf.pd.font.PDFont;
import org.verapdf.pd.font.cff.CFFFontProgram;
import org.verapdf.pd.font.cmap.CMap;
import org.verapdf.pd.font.truetype.TrueTypeFontProgram;
import org.verapdf.tools.resource.ASFileStreamCloser;

import java.io.IOException;

/**
 * Represents OpenType font.
 *
 * @author Sergey Shemyakov
 */
public class OpenTypeFontProgram implements FontProgram {

    private static final long CFF = 1128678944;     // "CFF " read as 4-byte unsigned number

    private boolean isCFF;
    private boolean isSymbolic;
    private boolean isSubset;
    private COSObject encoding;
    private ASInputStream source;
    private FontProgram font;
    private int numTables;
    private boolean attemptedParsing = false;
    private boolean successfullyParsed = false;
    private CMap externalCMap;

    /**
     * Constructor from stream, containing font data, and encoding details.
     *
     * @param source     is stream containing font data.
     * @param isSymbolic is true if font is marked as symbolic.
     * @param encoding   is value of /Encoding in font dictionary.
     */
    public OpenTypeFontProgram(ASInputStream source, boolean isCFF, boolean isSymbolic,
                               COSObject encoding, CMap externalCMap, boolean isSubset) {
        this.source = source;
        this.isCFF = isCFF;
        this.isSymbolic = isSymbolic;
        this.encoding = encoding;
        this.externalCMap = externalCMap;
        this.isSubset = isSubset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(int code) {
        return this.font.getWidth(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getWidth(String glyphName) {
        return this.font.getWidth(glyphName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCode(int code) {
        return this.font.containsCode(code);
    }

    @Override
    public boolean isAttemptedParsing() {
        return this.attemptedParsing;
    }

    @Override
    public boolean isSuccessfulParsing() {
        return this.successfullyParsed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseFont() throws IOException {
        if (!attemptedParsing) {
            attemptedParsing = true;
            if (!isCFF) {
                this.font = new TrueTypeFontProgram(source, isSymbolic, encoding);
                this.font.parseFont();
            } else {
                this.font = new CFFFontProgram(getCFFTable(),
                        PDFont.getEncodingMappingFromCOSObject(encoding),
                        externalCMap, isSubset);
                this.font.parseFont();
            }
            this.successfullyParsed = true;
        }
    }

    /**
     * @return CFF font or TrueType font, represented by this OpenType font.
     */
    public FontProgram getFont() {
        return font;
    }

    private ASInputStream getCFFTable() throws IOException {
        this.source = SeekableInputStream.getSeekableStream(this.source);
        this.readHeader();
        for (int i = 0; i < numTables; ++i) {
            long tabName = this.readULong();
            this.readULong();   // checksum
            long offset = this.readULong();
            long length = this.readULong();   // length
            if (tabName == CFF) {
                return ((SeekableInputStream) this.source).getStream(offset, length);
            }
        }
        throw new IOException("Can't locate \"CFF \" table in CFF OpenType font program.");
    }

    private void readHeader() throws IOException {
        this.source.skip(4);   // version
        this.numTables = this.readUShort();
        this.source.skip(6);
    }

    private int readUShort() throws IOException {
        int highOrder = (this.source.read() & 0xFF) << 8;
        return highOrder | (this.source.read() & 0xFF);
    }

    private long readULong() throws IOException {
        long res = readUShort();
        res = res << 16;
        return res | readUShort();
    }

    @Override
    public ASFileStreamCloser getFontProgramResource() {
        return this.font.getFontProgramResource();
    }
}
