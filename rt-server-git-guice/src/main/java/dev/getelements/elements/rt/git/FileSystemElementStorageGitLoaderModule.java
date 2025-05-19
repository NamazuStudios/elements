package dev.getelements.elements.rt.git;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.cluster.ApplicationAssetLoader;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.cluster.ApplicationAssetLoader.ELEMENT_STORAGE;

public class FileSystemElementStorageGitLoaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApplicationAssetLoader.class)
                .annotatedWith(named(ELEMENT_STORAGE))
                .toProvider(FileSystemScriptStorageGitLoaderProvider.class)
                .asEagerSingleton();
    }

}
