package org.verapdf.cos.visitor;

import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class IndirectWriter extends Writer {

	private Map<COSKey, COSKey> renum;

	public IndirectWriter(COSDocument document, String filename, boolean append,
						  long indirectOffset) throws IOException {
		super(document, filename, append, indirectOffset);
		this.renum = new HashMap<>();
		renum.put(new COSKey(0, 65535), new COSKey(0, 65535));
	}

	protected COSKey getKeyToWrite(final COSKey key) {
		if (!this.renum.containsKey(key)) {
			this.renum.put(key, new COSKey(this.renum.size(), 0));
		}
		return this.renum.get(key);
	}

}
