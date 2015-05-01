package com.namazustudios.promotion.client.rest;

import com.google.gwt.core.client.GWT;
import org.fusesource.restygwt.client.Resource;
import org.fusesource.restygwt.client.RestServiceProxy;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 4/30/15.
 */
public class ClientProvider implements Provider<Client> {

    @Override
    public Client get() {
        final Client client = GWT.create(Client.class);
        ((RestServiceProxy)client).setResource(new Resource("/api"));
        return client;
    }

}
