package com.namazustudios.socialengine.rt.xodus;

import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;

import static jetbrains.exodus.env.StoreConfig.*;

public class ResourceStores {

    private static final String PATHS = "com.namazustudios.socialengine.rt.xodus.paths";

    private static final String REVERSE_PATHS = "com.namazustudios.socialengine.rt.xodus.reverse.paths";

    private static final String RESOURCE_BLOCKS = "com.namazustudios.socialengine.rt.xodus.resource.blocks";

    private final Store paths;

    private final Store reversePaths;

    private final Store resourceBlocks;

    public ResourceStores(final Transaction transaction, final Environment environment) {
        this.paths = environment.openStore(PATHS, USE_EXISTING, transaction);
        this.reversePaths = environment.openStore(REVERSE_PATHS, USE_EXISTING, transaction);
        this.resourceBlocks = environment.openStore(RESOURCE_BLOCKS, USE_EXISTING, transaction);
    }

    private ResourceStores(final Store paths, final Store reversePaths, final Store resourceBlocks) {
        this.paths = paths;
        this.reversePaths = reversePaths;
        this.resourceBlocks = resourceBlocks;
    }

    public Store getPaths() {
        return paths;
    }

    public Store getReversePaths() {
        return reversePaths;
    }

    public Store getResourceBlocks() {
        return resourceBlocks;
    }

    public static ResourceStores create(final Transaction transaction, final Environment environment) {
        final var paths = environment.openStore(PATHS, WITHOUT_DUPLICATES_WITH_PREFIXING, transaction);
        final var reversePaths = environment.openStore(REVERSE_PATHS, WITHOUT_DUPLICATES, transaction);
        final var resourceBlocks = environment.openStore(RESOURCE_BLOCKS, WITHOUT_DUPLICATES_WITH_PREFIXING, transaction);
        return new ResourceStores(paths, reversePaths, resourceBlocks);
    }

}
