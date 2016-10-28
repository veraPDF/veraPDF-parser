package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;

import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements LZW decoding.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterLZWDecode extends ASBufferingInFilter {

    private Logger LOGGER = Logger.getLogger(COSFilterLZWDecode.class.getCanonicalName());

    private static final int CLEAR_TABLE_MARKER = 256;
    private static final int EOD = 257;
    private static final int INITIAL_LZW_TABLE_SIZE = 4096;
    private static final int MAX_LZW_TABLE_SIZE = 4096;

    private MemoryCacheImageInputStream bitStream;
    private List<byte[]> lzwTable;
    private byte[] leftoverData;
    private int codeLengthBits = 9;
    private long thisWord = -1;
    private long previousWord = -1;

    public COSFilterLZWDecode(ASInputStream stream) throws IOException {
        super(stream);
        this.bitStream = new MemoryCacheImageInputStream(stream);
        initLZWTable();
    }

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        int position = 0;
        int actualSize = Math.min(buffer.length, size);
        byte[] nextChunk;
        while (true) {
            nextChunk = getNextChunk();
            if (nextChunk == null) {
                return position == 0 ? -1 : position;
            }
            if (position + nextChunk.length > actualSize) {
                this.leftoverData = nextChunk;
                return position;
            }
            System.arraycopy(nextChunk, 0, buffer, position, nextChunk.length);
            position += nextChunk.length;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        this.bitStream.reset();
        this.codeLengthBits = 9;
        initLZWTable();
    }

    private byte[] getNextChunk() throws IOException {
        if (leftoverData != null) {
            byte[] res = Arrays.copyOf(leftoverData, leftoverData.length);
            this.leftoverData = null;
            return res;
        }
        try {
            thisWord = this.bitStream.readBits(codeLengthBits);
            if (thisWord == CLEAR_TABLE_MARKER) {
                this.codeLengthBits = 9;
                initLZWTable();
                return getNextChunk();
            } else if (thisWord == EOD) {
                return null;
            } else {
                byte[] res = getChunkFromLZWTable();
                this.codeLengthBits = calculateCodeLength();
                previousWord = thisWord;
                return res;
            }
        } catch (EOFException e) {
            LOGGER.log(Level.FINE, "Unexpected end of LZW data.");
            return null;
        }
    }

    private byte[] getChunkFromLZWTable() {
        if (thisWord < lzwTable.size()) {
            byte[] res = lzwTable.get((int) thisWord);
            if (previousWord != -1) {
                byte[] previous = lzwTable.get((int) previousWord);
                byte[] newWord = Arrays.copyOf(previous, previous.length + 1);
                newWord[previous.length] = res[0];
                if (lzwTable.size() < MAX_LZW_TABLE_SIZE) {
                    lzwTable.add(newWord);
                }
            }
            return res;
        } else {
            byte[] previous = lzwTable.get((int) previousWord);
            byte[] res = Arrays.copyOf(previous, previous.length + 1);
            res[previous.length] = previous[0];
            if (lzwTable.size() < MAX_LZW_TABLE_SIZE) {
                lzwTable.add(res);
            }
            return res;
        }
    }

    private int calculateCodeLength() {
        if (lzwTable.size() > 510) {
            return 10;
        } else if (lzwTable.size() > 1022) {
            return 11;
        } else if (lzwTable.size() > 2046) {
            return 12;
        }
        return 9;
    }

    private void initLZWTable() {
        this.lzwTable = new ArrayList<>(INITIAL_LZW_TABLE_SIZE);
        for (int i = 0; i < 256; ++i) {
            this.lzwTable.add(new byte[]{(byte) i});
        }
        this.lzwTable.add(null);
        this.lzwTable.add(null);
    }
}
