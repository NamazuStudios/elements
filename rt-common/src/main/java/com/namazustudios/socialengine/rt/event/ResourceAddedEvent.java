package com.namazustudios.socialengine.rt.event;

/**
 * Indicates a resource has been added to the server.
 * 
 * Created by patricktwohig on 8/25/15.
 */
@EventType
public class ResourceAddedEvent {

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
