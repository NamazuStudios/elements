package dev.getelements.elements.rt.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.InstanceMetadataContext;
import dev.getelements.elements.rt.SimpleInstanceMetadataContext;

public class SimpleInstanceMetadataContextModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(InstanceMetadataContext.class).to(SimpleInstanceMetadataContext.class).asEagerSingleton();
    }

}
