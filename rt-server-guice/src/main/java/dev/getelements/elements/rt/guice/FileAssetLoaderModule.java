package dev.getelements.elements.rt.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.AssetLoader;
import dev.getelements.elements.rt.FileAssetLoader;

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
