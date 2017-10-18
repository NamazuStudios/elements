package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.AssetNotFoundException;

import java.io.InputStream;

public class ClasspathAssetLoader extends AbstractAssetLoader {

    private final ClassLoader classLoader;

    public ClasspathAssetLoader() {
        this.classLoader = getClass().getClassLoader();
    }

    public ClasspathAssetLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public InputStream doOpen(final Path path) {

        final String classPath = path.toNormalizedPathString("/");
        final InputStream is = getClassLoader().getResourceAsStream(classPath);

        if (is == null) {
            throw new AssetNotFoundException("Not asset at path " + path);
        }

        return is;

    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

}
