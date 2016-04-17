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

	public static final String PDF_HEADER_DEFAULT = "%PDF-1.4";

	private PDCatalog catalog;
	private COSDocument document;

	public PDDocument() throws Exception {
		this.catalog = new PDCatalog();
		this.constructDocument();
	}

	public PDDocument(final String filename) throws Exception {
		this.catalog = new PDCatalog();
		this.document = new COSDocument(filename, this);
	}

	private void constructDocument() throws Exception {
		document = new COSDocument(this);
		document.setHeader(PDF_HEADER_DEFAULT);
		//initialize catalog
		this.getCatalog();
	}

	public void open(final String filename) throws Exception {
		this.close();

		document = new COSDocument(filename, this);
	}

	public void close() {
		document = null;

		catalog.clear();
		//this.info.clear;
	}

	public PDCatalog getCatalog() throws Exception {
		if (!catalog.empty() || document == null) {
			return catalog;
		}

		COSObject root = document.getTrailer().getRoot();

		if (!root.empty()) {
			catalog.setObject(root);
			return catalog;
		}

		root.setNameKey(ASAtom.TYPE, ASAtom.CATALOG);

		COSObject pages = new COSObject();
		pages.setNameKey(ASAtom.TYPE, ASAtom.PAGES);
		pages.setArrayKey(ASAtom.KIDS);
		pages.setIntegerKey(ASAtom.COUNT, 0);

		pages = COSIndirect.construct(root, document);
		root.setKey(ASAtom.PAGES, pages);

		root = COSIndirect.construct(root, document);
		document.getTrailer().setRoot(root);

		catalog.setObject(root);

		return catalog;
	}

	public COSDocument getDocument() {
		return document;
	}

	public int getNumberOfPages() throws Exception {
		return this.getCatalog().getPageTree().getPageCount();
	}

	public PDPage getPage(final int number) throws Exception {
		return this.getCatalog().getPageTree().getPage(number);
	}

	public void addPage(final PDPage page, final int number) throws Exception {
		if (document == null) {
			return;
		}

		final PDPageTree pages = this.getCatalog().getPageTree();
		page.getObject().setKey(ASAtom.PARENT, pages.getObject());
		if (pages.addPage(page, number)) {
			getCatalog().setKey(ASAtom.PAGES, pages.getRoot().getObject());
		}
		//TODO : check this
		final COSObject obj = pages.getObject();
		document.setObject(obj);
	}

	public PDPage newPage(final double[] bbox, final int insertAt) throws Exception {
		final PDPage page = new PDPage(bbox, document);
		this.addPage(page, insertAt);
		return page;
	}

	public void save() {
		//TODO : implement me
	}

	public void saveAs(final String fileName) throws Exception {
		final Writer out = new IndirectWriter(this.document, fileName, false);
		this.saveAs(out, fileName);
	}

	public void saveAs(final Writer out, final String filename) {
		if (document == null) {
			return;
		}

		//getInfo.setTime2();

		document.saveAs(out);
		out.close();
	}

}
