package com.namazustudios.socialengine.rt;

import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ClusterClientIndexContext implements IndexContext {

    @Override
    public Future<Stream<Listing>> listAsync(Path path, Consumer<Stream<Listing>> success, Consumer<Throwable> failure) {
        return null;
    }

    @Override
    public Future<Void> linkAsync(ResourceId resourceId, Path destination, Consumer<Void> success, Consumer<Throwable> failure) {
        return null;
    }

    @Override
    public Future<Void> linkPathAsync(Path source, Path destination, Consumer<Void> success, Consumer<Throwable> failure) {
        return null;
    }

    @Override
    public Future<Unlink> unlinkAsync(Path path, Consumer<Unlink> success, Consumer<Throwable> failure) {
        return null;
    }

}
