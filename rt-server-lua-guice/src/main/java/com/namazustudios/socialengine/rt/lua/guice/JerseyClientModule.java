package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class JerseyClientModule extends AbstractModule {

    @Override
    protected void configure() {
        // TODO Add additional support as the out of the box implementation is likely not enough
        bind(Client.class).toProvider(ClientBuilder::newClient).asEagerSingleton();
    }

}
