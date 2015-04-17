package com.namazustudios.promotion.client.guice;

import com.google.gwt.inject.client.AbstractGinModule;
import com.namazustudios.promotion.client.rest.Client;

/**
 * Created by patricktwohig on 4/16/15.
 */
public class RestyModule extends AbstractGinModule {

    @Override
    protected void configure() {
        binder().bind(Client.class);
    }

}
