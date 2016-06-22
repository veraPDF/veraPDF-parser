package org.verapdf.cos;

import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASOutputStream;
import org.verapdf.cos.xref.COSFilterRegistry;
import org.verapdf.pd.PDObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class COSFilters extends PDObject {

	private List<ASAtom> entries;

	public COSFilters() {
		super();
		this.entries = new ArrayList<>();
	}

	public COSFilters(final COSObject object) {
		this();
		setObject(object);
	}

	public ASInputStream getInputStream(ASInputStream inputStream,
										COSObject decodeParams) throws IOException {
		List<COSDictionary> decodeParameters = null;
		if(!decodeParams.equals(COSObject.getEmpty())) {
			if(decodeParams.getType().equals(COSObjType.COSDictT)) {
				decodeParameters = new ArrayList<>(1);
				decodeParameters.add((COSDictionary) decodeParams.get());
			} else if (decodeParams.getType().equals(COSObjType.COSArrayT)) {
				decodeParameters = new ArrayList<>(decodeParams.size());
				for(int i = 0; i < decodeParams.size(); ++i) {
					if(!decodeParams.at(i).getType().equals(COSObjType.COSDictT)) {
						throw new IOException("DecodeParams shall be a dictionary or array of dictionaries.");
					}
					decodeParameters.add((COSDictionary) decodeParams.at(i).get());
				}
			}
		}
		if(decodeParameters == null) {
			decodeParameters = new ArrayList<>(entries.size());
			for(int i = 0; i < entries.size(); ++i) {
				decodeParameters.add((COSDictionary) COSDictionary.construct().get());
			}
		}
		if(decodeParameters.size() != entries.size()) {
			throw new IOException("Amount of DecodeParams dictionaries and amount of decode filters in COSStream shall be equal.");
		}
		for (int i = 0; i < entries.size(); ++i) {
			inputStream = COSFilterRegistry.getDecodeFilter(entries.get(i),
					inputStream, decodeParameters.get(i));

			//TODO : if (!is.Get()) break;
		}
		return inputStream;
	}

	public ASOutputStream getOutputStream(ASOutputStream outputStream) throws IOException {
		for (ASAtom asAtom : entries) {
			outputStream = COSFilterRegistry.getEncodeFilter(asAtom, outputStream);

			//TODO : if (!is.Get()) break;
		}
		return outputStream;
	}

	public int size() {
		return this.entries.size();
	}

	public List<ASAtom> getFilters() {
		return entries;
	}

	protected void updateToObject() {
		COSObject filters = getObject();

		filters.clearArray();

		for (int i = 0; i < this.entries.size(); i++) {
			filters.add(COSName.construct(this.entries.get(i)));
		}
	}

	protected void updateFromObject() {
		COSObject filters = getObject();
		if(filters.getType().equals(COSObjType.COSArrayT)) {
			int size = filters.size();

			this.entries.clear();

			for (int i = 0; i < size; i++) {
				this.entries.add(filters.at(i).getName());
			}
		} else if (filters.getType().equals(COSObjType.COSNameT)) {
			this.entries.clear();
			this.entries.add(filters.getName());
		}
	}

}
