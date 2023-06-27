package dev.getelements.elements.rt;

/**
 * Created by patricktwohig on 8/8/15.
 */
public class SimpleEventHeader implements EventHeader {

    private String path;

    private String name;

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
