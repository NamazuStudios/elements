package com.namazustudios.socialengine.rt.event;

/**
 * Indicates a resource has been moved from one path to another.
 *
 * Created by patricktwohig on 8/25/15.
 */
public class ResourceMovedEvent {

    private String oldPath;

    private String newPath;

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String getNewPath() {
        return newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

}
