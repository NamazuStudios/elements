package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.*;

import javax.inject.Inject;
import java.util.function.Consumer;

public class XodusResourceContext implements ResourceContext {

    public static final String RESOURCE_ENVIRONMENT = "com.namazustudios.socialengine.rt.xodus.resource";

    private SimpleResourceContext delegate;

    @Override
    public void start() {
        getDelegate().start();
    }

    @Override
    public void stop() {
        getDelegate().stop();
    }

    @Override
    public void createAttributesAsync(final Consumer<ResourceId> success, final Consumer<Throwable> failure,
                                      final String module, Path path, Attributes attributes, final Object... args) {
        getDelegate().createAttributesAsync(success, failure, module, path, attributes, args);
    }

    @Override
    public void invokeAsync(final Consumer<Object> success, final Consumer<Throwable> failure,
                            final ResourceId resourceId, final String method, final Object... args) {
        getDelegate().invokeAsync(success, failure, resourceId, method, args);
    }

    @Override
    public void invokePathAsync(final Consumer<Object> success, final Consumer<Throwable> failure,
                                final Path path, final String method, final Object... args) {
        getDelegate().invokePathAsync(success, failure, path, method, args);
    }

    @Override
    public void destroyAsync(final Consumer<Void> success, final Consumer<Throwable> failure,
                             final ResourceId resourceId) {
        getDelegate().destroyAsync(success, failure, resourceId);
    }

    @Override
    public void destroyAllResourcesAsync(final Consumer<Void> success, final Consumer<Throwable> failure) {
        getDelegate().destroyAllResourcesAsync(success, failure);
    }

    public SimpleResourceContext getDelegate() {
        return delegate;
    }

    @Inject
    public void setDelegate(SimpleResourceContext delegate) {
        this.delegate = delegate;
    }

}
