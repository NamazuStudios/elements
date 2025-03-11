package dev.getelements.elements.rt.remote.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.cluster.id.InstanceId;

public class RandomInstanceIdModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(InstanceId.class).toProvider(InstanceId::randomInstanceId).asEagerSingleton();
    }

}
