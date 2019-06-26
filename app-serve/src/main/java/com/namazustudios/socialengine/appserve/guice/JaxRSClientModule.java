package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class JaxRSClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Client.class).toProvider(() -> ClientBuilder
            .newBuilder()
            .register(JacksonFeature.class)
            .build()
        ).asEagerSingleton();
    }

}
