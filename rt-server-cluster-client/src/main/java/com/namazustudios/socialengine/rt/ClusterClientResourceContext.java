package com.namazustudios.socialengine.rt;

import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ClusterClientResourceContext implements ResourceContext {

    @Override
    public Future<ResourceId> createAttributesAsync(Consumer<ResourceId> success, Consumer<Throwable> failure, String module, Path path, Attributes attributes, Object... args) {
        return null;
    }

    @Override
    public Future<Object> invokeAsync(Consumer<Object> success, Consumer<Throwable> failure, ResourceId resourceId, String method, Object... args) {
        return null;
    }

    @Override
    public Future<Object> invokePathAsync(Consumer<Object> success, Consumer<Throwable> failure, Path path, String method, Object... args) {
        return null;
    }

    @Override
    public Future<Void> destroyAsync(Consumer<Void> success, Consumer<Throwable> failure, ResourceId resourceId) {
        return null;
    }

    @Override
    public Future<Void> destroyAllResourcesAsync(Consumer<Void> success, Consumer<Throwable> failure) {
        return null;
    }

}
