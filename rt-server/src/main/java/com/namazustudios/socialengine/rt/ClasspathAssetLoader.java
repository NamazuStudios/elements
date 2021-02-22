package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.AssetNotFoundException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;

public class ClasspathAssetLoader extends AbstractAssetLoader {

    public static final String PACKAGE_ROOT = "com.namazustudios.socialengine.rt.classpath.asset.loader.package.root";

    private final ClassLoader classLoader;

    private Path root = Path.fromPathString("");

    public ClasspathAssetLoader() {
        this.classLoader = getClass().getClassLoader();
    }

    @Override
    public InputStream doOpen(final Path path) {
        final var cp = new Path(root, path).toNormalizedPathString("/");
        final var is = getClassLoader().getResourceAsStream(cp);
        if (is == null) throw new AssetNotFoundException("Not asset at path " + path);
        return is;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Inject
    public void setPkg(@Named(PACKAGE_ROOT) final String pkg) {
        root = Path.fromPathString(pkg,".");
    }

}
