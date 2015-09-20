package com.namazustudios.socialengine.rt.guice.example.Client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.ClientContainer;
import com.namazustudios.socialengine.rt.mina.guice.MinaClientModule;
import com.namazustudios.socialengine.rt.mina.guice.MinaDefaultClientModule;

/**
 * Created by patricktwohig on 9/11/15.
 */
public class ClientMain {


    public static void main(final String[] args) throws Exception {

        final Injector injector = Guice.createInjector(
                new MinaClientModule(),
                new MinaDefaultClientModule()
        );

        final ClientContainer clientContainer = injector.getInstance(ClientContainer.class);

    }

}
