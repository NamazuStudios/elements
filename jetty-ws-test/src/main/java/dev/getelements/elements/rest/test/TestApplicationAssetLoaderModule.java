package dev.getelements.elements.rest.test;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.ApplicationAssetLoader;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.ApplicationAssetLoader.ELEMENT_STORAGE;

public class TestApplicationAssetLoaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApplicationAssetLoader.class)
                .annotatedWith(named(ELEMENT_STORAGE))
                .to(TestApplicationAssetLoader.class)
                .asEagerSingleton();
    }

}
