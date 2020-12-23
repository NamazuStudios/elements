package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.security.SecurityManifest;
import com.namazustudios.socialengine.rt.manifest.startup.StartupManifest;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleManifestContext implements ManifestContext {

    private Provider<ManifestLoader> manifestLoaderProvider;

    private final AtomicReference<ManifestLoader> loaderAtomicReference = new AtomicReference<>();

    @Override
    public void start() {

        final var loader = getManifestLoaderProvider().get();

        try {
            if (!loaderAtomicReference.compareAndSet(null, loader)) {
                throw new IllegalStateException("Already running!");
            }
        } catch (Exception ex) {
            loader.close();
        }

    }

    @Override
    public void stop() {
        final var loader = loaderAtomicReference.getAndSet(null);
        if (loader == null) throw new IllegalStateException("Not running.");
        loader.close();
    }

    @Override
    public ModelManifest getModelManifest() {
        return getCurrentLoader().getModelManifest();
    }

    @Override
    public HttpManifest getHttpManifest() {
        return getCurrentLoader().getHttpManifest();
    }

    @Override
    public SecurityManifest getSecurityManifest() {
        return getCurrentLoader().getSecurityManifest();
    }

    @Override
    public StartupManifest getStartupManifest() {
        return getCurrentLoader().getStartupManifest();
    }

    private final ManifestLoader getCurrentLoader() {
        final var loader = loaderAtomicReference.get();
        if (loader == null) throw new IllegalStateException("not running.");
        return loader;
    }

    public Provider<ManifestLoader> getManifestLoaderProvider() {
        return manifestLoaderProvider;
    }

    @Inject
    public void setManifestLoaderProvider(Provider<ManifestLoader> manifestLoaderProvider) {
        this.manifestLoaderProvider = manifestLoaderProvider;
    }

}
