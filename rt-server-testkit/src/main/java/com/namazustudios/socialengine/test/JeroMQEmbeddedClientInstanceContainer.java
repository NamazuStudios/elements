package com.namazustudios.socialengine.test;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextFactoryModule;
import com.namazustudios.socialengine.rt.remote.guice.SimpleInstanceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQInstanceConnectionServiceModule;

public class JeroMQEmbeddedClientInstanceContainer extends JeroMQEmbeddedInstanceContainer {

    public JeroMQEmbeddedClientInstanceContainer() {
        withInstanceModules(new AbstractModule() {
            @Override
            protected void configure() {
                install(new SimpleInstanceModule());
                install(new ClusterContextFactoryModule());
                install(new JeroMQInstanceConnectionServiceModule().withDefaultRefreshInterval());
            }
        });
    }

}
