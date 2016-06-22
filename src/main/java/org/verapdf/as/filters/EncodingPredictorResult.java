package org.verapdf.as.filters;

/**
 * Class represents value, returned by {@link EncodingPredictorDecode}. This value
 * contains byte array of predicted data and byte array of unpredicted data, that
 * are bytes remained after splitting input into integer number of lines.
 *
 * @author Sergey Shemyakov
 */
public class EncodingPredictorResult {
    byte[] predictedData;
    byte[] unpredictedData;

    public EncodingPredictorResult(byte[] predictedData, byte[] unpredictedData) {
        this.predictedData = predictedData;
        this.unpredictedData = unpredictedData;
    }

    /**
     * @return byte array of successfully processed data.
     */
    public byte[] getPredictedData() {
        return this.predictedData;
    }

    /**
     * @return byte array of remained unpredicted data. It's length is always
     * less than <code>columns</code> value in <code>DecodeParams</code>.
     */
    public byte[] getUnpredictedData() {
        return this.unpredictedData;
    }
}
