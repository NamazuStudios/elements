package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.AbstractModule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class JaxRSClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Client.class).toProvider(ClientBuilder::newClient).asEagerSingleton();
    }

}
