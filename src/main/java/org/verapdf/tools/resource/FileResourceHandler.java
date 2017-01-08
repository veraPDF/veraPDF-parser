package org.verapdf.tools.resource;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Shemyakov
 */
public class FileResourceHandler implements Closeable {

    private List<Closeable> resources;

    public FileResourceHandler() {
        this.resources = new ArrayList<>();
    }

    public void addResource(ASFileStreamCloser obj) {
        Closeable resource = obj.getStream();
        if (resource != null && !resources.contains(resource)) {
            resources.add(obj);
        }
    }

    public void addResource(Closeable res) {
        if (res != null && !resources.contains(res)) {
            resources.add(res);
        }
    }

    @Override
    public void close() throws IOException {
        for (Closeable obj : resources) {
            obj.close();
        }
    }
}
