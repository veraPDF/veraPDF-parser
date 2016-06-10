package org.verapdf.cos.xref;

import org.verapdf.cos.COSKey;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Timur Kamalov
 */
public class COSXRefTable {

    private List<COSKey> all;
    private List<COSKey> n;
    private List<COSKey> f;
    private int size;

    public COSXRefTable() {
        this.all = new ArrayList<COSKey>();
        this.n = new ArrayList<COSKey>();
        this.f = new ArrayList<COSKey>();
        this.size = 1;
    }

    public void set(final List<COSKey> keys) {
        this.all = keys;
        n.clear();
        f.clear();
        int lastIndex = keys.size() - 1;
        this.size = keys.isEmpty() ? 1 : keys.get(lastIndex).getNumber() + 1;
    }

    public COSKey next() {
        return new COSKey(this.size++);
    }

    public void newKey(final COSKey key) {
        this.all.add(key);
        this.n.add(key);
    }

    public void newKey(final List<COSKey> key) {
        this.all.addAll(key);
        this.n.addAll(key);
    }

    public List<COSKey> getAllKeys() {
        return this.all;
    }

}
