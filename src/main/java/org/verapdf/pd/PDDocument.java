package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDocument;
import org.verapdf.cos.COSIndirect;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.visitor.IndirectWriter;
import org.verapdf.cos.visitor.Writer;

/**
 * @author Timur Kamalov
 */
public class PDDocument {

	private PDCatalog catalog;
	private COSDocument document;

	public PDDocument() throws Exception {
		this.catalog = new PDCatalog();
		constructDocument();
	}

	public PDDocument(final String fileName) throws Exception {
		this.catalog = new PDCatalog();
		this.document = new COSDocument(fileName, this);
	}

	private void constructDocument() throws Exception {
		this.document = new COSDocument(this);
		this.document.setHeader("%PDF-1.4");
		//initialize catalog
		getCatalog();
	}

	public void open(final String filename) throws Exception {
		close();

		this.document = new COSDocument(filename, this);
	}

	public void close() {
		this.document = null;

		this.catalog.clear();

		//this.info.clear;
	}

	public PDCatalog getCatalog() throws Exception {
		if (!this.catalog.empty() || this.document == null) {
			return this.catalog;
		}

		COSObject root = this.document.getTrailer().getRoot();

		if (!root.empty()) {
			this.catalog.setObject(root);
			return this.catalog;
		}

		root.setNameKey(ASAtom.TYPE, ASAtom.CATALOG);

		COSObject pages = new COSObject();
		pages.setNameKey(ASAtom.TYPE, ASAtom.PAGES);
		pages.setArrayKey(ASAtom.KIDS);
		pages.setIntegerKey(ASAtom.COUNT, 0);

		pages = COSIndirect.construct(root, this.document);
		root.setKey(ASAtom.PAGES, pages);

		root = COSIndirect.construct(root, this.document);
		this.document.getTrailer().setRoot(root);

		this.catalog.setObject(root);

		return catalog;
	}

	public int getNumberOfPages() throws Exception {
		return getCatalog().getPageTree().getPageCount();
	}

	public PDPage getPage(int number) throws Exception {
		return getCatalog().getPageTree().getPage(number);
	}

	public void addPage(final PDPage page, final int number) throws Exception {
		if (this.document == null) {
			return;
		}

		PDPageTree pages = getCatalog().getPageTree();
		page.getObject().setKey(ASAtom.PARENT, pages.getObject());
		if (pages.addPage(page, number)) {
			getCatalog().setKey(ASAtom.PAGES, pages.getRoot().getObject());
		}
		COSObject obj = pages.getObject();
		this.document.setObject(obj);
	}

	public PDPage newPage(final double[] bbox, final int insertAt) throws Exception {
		PDPage page = new PDPage(bbox, document);
		addPage(page, insertAt);
		return page;
	}

	public void save() {
	}

	public void saveAs(final String fileName) throws Exception {
		Writer out = new IndirectWriter(this.document, fileName, false);
		saveAs(out, fileName);
	}

	public void saveAs(final Writer out, final String filename) {
		if (this.document == null) {
			return;
		}

		//getInfo.setTime2();

		this.document.saveAs(out);
		out.close();
	}

}
