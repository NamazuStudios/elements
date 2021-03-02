package com.namazustudios.socialengine.rt.xodus;

import jetbrains.exodus.env.Store;

public class ResourceStores {

    private final Store paths;

    private final Store reversePaths;

    private final Store resources;

    public ResourceStores(Store paths, Store reversePaths, Store resources) {
        this.paths = paths;
        this.reversePaths = reversePaths;
        this.resources = resources;
    }

    public Store getPaths() {
        return paths;
    }

    public Store getReversePaths() {
        return reversePaths;
    }

    public Store getResources() {
        return resources;
    }

}
