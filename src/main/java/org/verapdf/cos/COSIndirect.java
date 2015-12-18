package org.verapdf.cos;

/**
 * Created by Timur on 12/17/2015.
 */
public class COSIndirect extends COSBase {

    private COSKey key;
    private COSDocument document;
    private COSObject child;

    protected COSIndirect() {
        super();
        this.key = new COSKey();
        this.document = new COSDocument(null);
        this.child = new COSObject();
    }

    protected COSIndirect(final COSKey key, final COSDocument document) {
        super();
        this.key = key;
        this.document = document;
        this.child = new COSObject();
    }

    protected COSIndirect(final COSObject value, final COSDocument document) {
        super();
        this.key = new COSKey();
        this.document = document;
        this.child = new COSObject();

        if (document == null) {
            this.child = value;
        } else {
            this.key = this.document.setObject(value);
        }
    }

    public static COSObject construct(final COSKey value) {
        return construct(value, null);
    }

    public static COSObject construct(final COSKey value, final COSDocument doc) {
        return new COSObject(new COSIndirect(value, doc));
    }

    public COSObjType getType() {
        return getDirect().getType();
    }

    public COSObject getDirect() {
        return this.document != null ? this.document.getObject(key) : this.child;
    }

}
