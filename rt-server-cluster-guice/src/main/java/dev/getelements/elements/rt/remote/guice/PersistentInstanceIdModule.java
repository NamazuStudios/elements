package dev.getelements.elements.rt.remote.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.rt.remote.PersistentInstanceIdProvider;

public class PersistentInstanceIdModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(InstanceId.class).toProvider(PersistentInstanceIdProvider.class);
    }

}
