package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ClasspathAssetLoader;

import static com.google.inject.name.Names.named;

public class ClasspathAssetLoaderModule extends PrivateModule {

    private Runnable bindPackageRoot = () -> {};

    @Override
    protected void configure() {
        bindPackageRoot.run();
        bind(AssetLoader.class).to(ClasspathAssetLoader.class);
        expose(AssetLoader.class);
    }

    public ClasspathAssetLoaderModule withDefaultPackageRoot() {
        return withPackageRoot("");
    }

    public ClasspathAssetLoaderModule withPackageRoot(final String packageRoot) {
        bindPackageRoot = () -> bind(String.class)
            .annotatedWith(named(ClasspathAssetLoader.PACKAGE_ROOT))
            .toInstance(packageRoot);
        return this;
    }

}
