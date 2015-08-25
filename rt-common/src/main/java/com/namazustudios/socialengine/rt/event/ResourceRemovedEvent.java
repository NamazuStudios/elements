package com.namazustudios.socialengine.rt.event;

/**
 *Indicates a resource has been removed from the server completely.
 *
 * Created by patricktwohig on 8/25/15.
 */
public class ResourceRemovedEvent {

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
