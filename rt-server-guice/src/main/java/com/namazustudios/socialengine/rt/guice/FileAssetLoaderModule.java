package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.FileAssetLoader;

import java.io.File;

public class FileAssetLoaderModule extends AbstractModule {

    private final FileAssetLoader fileAssetLoader;

    public FileAssetLoaderModule(final File rootDirectory) {
        this.fileAssetLoader = new FileAssetLoader(rootDirectory);
    }

    @Override
    protected void configure() {
        final AssetLoader rootAssetLoader = fileAssetLoader.getReferenceCountedView();
        bind(AssetLoader.class).toProvider(rootAssetLoader::getReferenceCountedView);
    }

}
