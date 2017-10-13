package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.FileAssetLoader;

import java.io.File;

import static com.namazustudios.socialengine.rt.AssetLoader.*;

public class RTFileAssetLoaderModule extends AbstractModule {

    private final FileAssetLoader fileAssetLoader;

    public RTFileAssetLoaderModule(final File rootDirectory) {
        this.fileAssetLoader = new FileAssetLoader(rootDirectory);
    }

    @Override
    protected void configure() {
        final AssetLoader rootAssetLoader = fileAssetLoader.getReferenceCountedView();
        bind(AssetLoader.class).toProvider(rootAssetLoader::getReferenceCountedView);
    }

}
