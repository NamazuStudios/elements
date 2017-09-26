package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.FileAssetLoader;

import java.io.File;

import static com.namazustudios.socialengine.rt.AssetLoader.*;

public class FileAssetLoaderModule extends AbstractModule {

    private final FileAssetLoader fileAssetLoader;

    public FileAssetLoaderModule(File rootDirectory) {
        this.fileAssetLoader = new FileAssetLoader(rootDirectory);
    }

    @Override
    protected void configure() {

        bind(AssetLoader.class).annotatedWith(Names.named(ROOT)).toInstance(fileAssetLoader);

        final AssetLoader view = fileAssetLoader.getReferenceCountedView();
        bind(AssetLoader.class).toProvider(view::getReferenceCountedView);

    }

}
