package dev.getelements.elements.rest.test;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.cluster.ApplicationAssetLoader;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.cluster.ApplicationAssetLoader.ELEMENT_STORAGE;

public class TestApplicationAssetLoaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApplicationAssetLoader.class)
                .annotatedWith(named(ELEMENT_STORAGE))
                .to(TestApplicationAssetLoader.class)
                .asEagerSingleton();
    }

}
