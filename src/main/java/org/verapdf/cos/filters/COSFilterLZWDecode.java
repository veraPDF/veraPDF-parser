package org.verapdf.cos.filters;

import org.verapdf.as.ASAtom;
import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSDictionary;

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

    private static final int BITS_12 = 12;
    private static final int BITS_11 = 11;
    private static final int BITS_10 = 10;
    private static final int BITS_9 = 9;

    private static final int SIZE_THRESHOLD_10_BITS = 512;
    private static final int SIZE_THRESHOLD_11_BITS = 1024;
    private static final int SIZE_THRESHOLD_12_BITS = 2048;

    private MemoryCacheImageInputStream bitStream;
    private List<byte[]> lzwTable;
    private byte[] leftoverData;
    private int codeLengthBits = 9;
    private int earlyChange;
    private long thisWord = -1;
    private long previousWord = -1;

    public COSFilterLZWDecode(ASInputStream stream, COSDictionary decodeParams) throws IOException {
        super(stream);
        Long earlyChangeFromDecodeParams = decodeParams.getIntegerKey(ASAtom.EARLY_CHANGE);
        // 0 and 1 are only possible values.
        // In other cases we use default value 1.
        if (earlyChangeFromDecodeParams == null ||
                earlyChangeFromDecodeParams.intValue() != 0) {
            this.earlyChange = 1;
        } else {
            this.earlyChange = 0;
        }
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
                int toWrite = actualSize - position;
                byte[] newLeftover = new byte[nextChunk.length - toWrite];
                System.arraycopy(nextChunk, 0, buffer, position, toWrite);
                position += toWrite;
                System.arraycopy(nextChunk, toWrite, newLeftover, 0, nextChunk.length - toWrite);
                this.leftoverData = newLeftover;
                return position;
            }
            System.arraycopy(nextChunk, 0, buffer, position, nextChunk.length);
            position += nextChunk.length;
        }
    }

    @Override
    public int skip(int size) throws IOException {
        byte[] buf = new byte[BF_BUFFER_SIZE];
        int read = this.read(buf, size);
        while (read != size) {
            read += this.read(buf, size - read);
        }
        return read;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        this.bitStream = new MemoryCacheImageInputStream(this.getInputStream());
        this.codeLengthBits = 9;
        this.leftoverData = null;
        this.previousWord = -1;
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

    private byte[] getChunkFromLZWTable() throws IOException {
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
            if (previousWord == -1) {
                throw new IOException("Error in decoding LZW: first symbol in message can't be decoded.");
            }
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
        int size = lzwTable.size() + earlyChange;
        if (size >= SIZE_THRESHOLD_12_BITS) {
            return BITS_12;
        } else if (size >= SIZE_THRESHOLD_11_BITS) {
            return BITS_11;
        } else if (size >= SIZE_THRESHOLD_10_BITS ) {
            return BITS_10;
        }
        return BITS_9;
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
