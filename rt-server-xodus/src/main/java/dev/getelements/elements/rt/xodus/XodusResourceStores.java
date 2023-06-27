package dev.getelements.elements.rt.xodus;

import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jetbrains.exodus.env.StoreConfig.*;

public class XodusResourceStores {

    public static final String PATHS = "dev.getelements.elements.rt.xodus.paths";

    public static final String REVERSE_PATHS = "dev.getelements.elements.rt.xodus.reverse.paths";

    public static final String RESOURCE_BLOCKS = "dev.getelements.elements.rt.xodus.resource.blocks";

    public static final List<String> ALL_STORES = List.of(PATHS, REVERSE_PATHS, RESOURCE_BLOCKS);

    private final Store paths;

    private final Store reversePaths;

    private final Store resourceBlocks;

    public XodusResourceStores(final Transaction transaction, final Environment environment) {
        this.paths = environment.openStore(PATHS, USE_EXISTING, transaction);
        this.reversePaths = environment.openStore(REVERSE_PATHS, USE_EXISTING, transaction);
        this.resourceBlocks = environment.openStore(RESOURCE_BLOCKS, USE_EXISTING, transaction);
    }

    private XodusResourceStores(final Store paths, final Store reversePaths, final Store resourceBlocks) {
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

    public static XodusResourceStores create(final Transaction transaction) {
        return create(transaction, transaction.getEnvironment());
    }

    public static XodusResourceStores create(final Transaction transaction, final Environment environment) {
        final var paths = environment.openStore(PATHS, WITHOUT_DUPLICATES_WITH_PREFIXING, transaction);
        final var reversePaths = environment.openStore(REVERSE_PATHS, WITH_DUPLICATES, transaction);
        final var resourceBlocks = environment.openStore(RESOURCE_BLOCKS, WITHOUT_DUPLICATES_WITH_PREFIXING, transaction);
        return new XodusResourceStores(paths, reversePaths, resourceBlocks);
    }

}
