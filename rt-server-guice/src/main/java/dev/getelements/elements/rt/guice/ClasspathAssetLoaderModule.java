package dev.getelements.elements.rt.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.AssetLoader;
import dev.getelements.elements.rt.ClasspathAssetLoader;

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
