package org.verapdf.cos.visitor;

import org.verapdf.as.ASAtom;
import org.verapdf.as.ASCharsets;
import org.verapdf.as.exceptions.StringExceptions;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.*;
import org.verapdf.cos.xref.COSXRefEntry;
import org.verapdf.cos.xref.COSXRefInfo;
import org.verapdf.cos.xref.COSXRefRange;
import org.verapdf.cos.xref.COSXRefSection;
import org.verapdf.io.InternalOutputStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class Writer implements IVisitor {

	protected InternalOutputStream os;

	protected COSXRefInfo info;

	protected COSDocument document;

	protected List<COSKey> toWrite;
	protected List<COSKey> written;

	private final NumberFormat formatXrefOffset = new DecimalFormat("0000000000");
	private final NumberFormat formatXrefGeneration = new DecimalFormat("00000");

	public static final String EOL = "\r\n";

	public Writer(final COSDocument document, final String filename) throws Exception {
		this(document, filename, true);
	}

	public Writer(final COSDocument document, final String filename, final boolean append) throws Exception {
		this.document = document;
		this.os = new InternalOutputStream(filename);
		this.info = new COSXRefInfo();

		this.toWrite = new ArrayList<COSKey>();
		this.written = new ArrayList<COSKey>();

		if (append) {
			this.os.seekEnd();
		}
	}

	public void visitFromBoolean(COSBoolean obj) {
		try {
			this.write(obj.get());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visitFromInteger(COSInteger obj) {
		try {
			this.write(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visitFromReal(COSReal obj) {
		try {
			this.write(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visitFromString(COSString obj) {
		try {
			this.write(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visitFromName(COSName obj) {
		try {
			this.write(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visitFromArray(COSArray obj) {
		try {
			this.write("[");
			for (int i = 0; i < obj.size(); i++) {
				this.write(obj.at(i));
				this.write(" ");
			}
			this.write("]");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visitFromDictionary(COSDictionary obj) {
		try {
			this.write("<<");
			for (Map.Entry<ASAtom, COSObject> entry : obj.getEntrySet()) {
				this.write(entry.getKey());
				this.write(" ");
				this.write(entry.getValue());
				this.write(" ");
			}
			this.write(">>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visitFromStream(COSStream obj) {
		long length = 0;

		ASInputStream in = obj.getData();

		if (obj.getFilterFlags() == COSStream.FilterFlags.DECODE ||
				obj.getFilterFlags() == COSStream.FilterFlags.DECRYPT_AND_DECODE) {
			//TODO : Decode
		}

		visitFromDictionary(obj);

		try {
			this.write(EOL);
			this.write("stream");
			this.write(EOL);

			length = getOffset();

			byte[] buffer = new byte[1024];
			int count = 0;

			in.reset();

			while(true) {
				count = in.read(buffer, 1024);
				if (count == 0) {
					break;
				}
				this.os.write(buffer, count);
			}

			length = getOffset() - length;
			obj.setLength(length);

			this.write(EOL);
			this.write("endstream");
		} catch (IOException e) {
			throw new RuntimeException(StringExceptions.WRITE_ERROR);
		}
	}

	public void visitFromNull(COSNull obj) {
		try {
			this.write("null");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visitFromIndirect(COSIndirect obj) {
		try {
			COSKey key = obj.getKey();

			if (key.equals(new COSKey())) {
				COSObject direct = obj.getDirect();
				key = this.document.setObject(direct);
				obj.setKey(key, this.document);
				addToWrite(key);
			}

			this.write(key);
			this.write(" R");
		} catch (IOException e) {
			//TODO : ASException, message
			throw new RuntimeException(e.getMessage());
		}
	}

	public void writeHeader(final String header) {
		try {
			this.write(header);
			this.write(EOL);

			String comment = new String(new char[] { 0xE2, 0xE3, 0xCF, 0xD3 });
			this.write(comment);
			this.write(EOL);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addToWrite(final COSKey key) {
		this.toWrite.add(key);
	}

	public void addToWrite(final List<COSKey> keys) {
		this.toWrite.addAll(keys);
	}

	public void writeBody() {
		try {
			while (!this.toWrite.isEmpty()) {
				final COSKey key = this.toWrite.get(0);

				this.toWrite.remove(0);
				this.written.add(key);

				write(key, this.document.getObject(key));
			}
		} catch (IOException e) {
			throw new RuntimeException(StringExceptions.WRITE_ERROR);
		}
	}

	public void freeObjects(final Map<COSKey, Long> keys) {
		for (Map.Entry<COSKey, Long> entry : keys.entrySet()) {
			addXRef(entry.getKey(), entry.getValue(), 'f');
		}
	}

	public void setTrailer(final COSTrailer trailer) {
		setTrailer(trailer, 0);
	}

	public void setTrailer(final COSTrailer trailer, final long prev) {
		COSObject element = new COSObject();
		COSCopier copier = new COSCopier(element);
		trailer.getObject().accept(copier);

		this.info.getTrailer().setObject(element);

		this.info.getTrailer().setPrev(prev);

		if (prev == 0) {
			this.info.getTrailer().removeKey(ASAtom.ID);
		}
	}

	public void writeXRefInfo() {
		try {
			this.info.setStartXRef(getOffset());

			this.info.getTrailer().setSize(this.info.getXRefSection().next());

			generateID();

			this.write("xref"); this.write(EOL); this.write(info.getXRefSection());
			this.write("trailer"); this.write(EOL); this.write(this.info.getTrailer().getObject()); this.write(EOL);
			this.write("startxref"); this.write(EOL); this.write(this.info.getStartXRef()); this.write(EOL);
			this.write("%%EOF"); this.write(EOL);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public COSXRefInfo getXRefInfo() {
		return this.info;
	}

	public void clear() {
		try {
			this.info = new COSXRefInfo();

			this.toWrite.clear();
			this.written.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected long getOffset() {
		try {
			return this.os.tellp();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	protected void write(final COSKey key, final COSObject object) throws IOException {
		addXRef(key);
		this.write(key);
		this.write(" obj");
		this.write(EOL);
		this.write(object);
		this.write(EOL);
		this.write("endobj");
		this.write(EOL);
	}

	protected void generateID() {
		// TODO : finish this method
		Long idTime = System.currentTimeMillis();
		MessageDigest md5;
		try	{
			md5 = MessageDigest.getInstance("MD5");
			md5.update(Long.toString(idTime).getBytes("ISO-8859-1"));
			COSObject idString = COSString.construct(String.valueOf(md5.digest()), true);

			this.info.getTrailer().setID(idString);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected COSKey getKeyToWrite(final COSKey key) {
		return key;
	}

	protected void addXRef(final COSKey key, final long offset, final char free) {
		this.info.getXRefSection().add(getKeyToWrite(key), offset, free);
	}

	public void addXRef(final COSKey key) throws IOException {
		addXRef(key, getOffset(), 'n');
	}

	protected void write(final boolean value) throws IOException {
		this.os.write(value);
	}

	protected void write(final int value) throws IOException {
		this.os.write(String.valueOf(value));
	}

	protected void write(final long value) throws IOException {
		this.os.write(String.valueOf(value));
	}

	protected void write(final char value) throws IOException {
		this.os.write(value);
	}

	protected void write(final String value) throws IOException {
		this.os.write(value);
	}

	protected void write(final ASAtom value) throws IOException {
		this.os.write(value.toString());
	}

	protected void write(final COSKey value) throws IOException {
		final COSKey newKey = getKeyToWrite(value);
		this.write(newKey.getNumber()); this.write(" "); this.write(newKey.getGeneration());
	}

	protected void write(final COSObject value) throws IOException {
		value.accept(this);
	}

	protected void write(final COSXRefRange value) throws IOException {
		os.write(String.valueOf(value.start)).write(" ").write(String.valueOf(value.count)).write(EOL);
	}

	protected void write(final COSXRefEntry value) throws IOException {
		String offset = formatXrefOffset.format(value.offset);
		String generation = formatXrefGeneration.format(value.generation);
		os.write(offset.getBytes(ASCharsets.ISO_8859_1));
		os.write(" ");
		os.write(generation.getBytes(ASCharsets.ISO_8859_1));
		os.write(" ");
		os.write(String.valueOf(value.free).getBytes(ASCharsets.US_ASCII));
		os.write(EOL);
	}

	protected void write(final COSXRefSection value) throws IOException {
		List<COSXRefRange> range = value.getRange();
		for (int i = 0; i < range.size(); i++) {
			write(range.get(i));
			for (int j = range.get(i).start; j < range.get(i).next(); j++) {
				this.write(value.getEntry(j));
			}
		}
	}

}
