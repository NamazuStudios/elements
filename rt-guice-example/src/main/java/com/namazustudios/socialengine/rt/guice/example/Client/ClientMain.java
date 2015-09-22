package com.namazustudios.socialengine.rt.guice.example.Client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.mina.guice.MinaReliableClientModule;
import com.namazustudios.socialengine.rt.mina.guice.MinaDefaultClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by patricktwohig on 9/11/15.
 */
public class ClientMain {

    private static final Logger LOG = LoggerFactory.getLogger(ClientMain.class);

    public static void main(final String[] args) throws Exception {

        final Injector injector = Guice.createInjector(
                new MinaDefaultClientModule()
        );

        // First we get our context for connecting to the server.

        LOG.info("Obtaining ClientContainer instance.");
        final ClientContainer clientContainer = injector.getInstance(ClientContainer.class);

        final String hostname = args.length > 0 ? args[0] : "localhost";
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, Constants.DEFAULT_PORT);
        LOG.info("Connecting to server at {}", inetSocketAddress);

        // Next we must connect to the server and start making requests.

        try (final ClientContainer.ConnectedInstance connectedInstance = clientContainer.connect(inetSocketAddress)) {

            // Before we make the request, we must first ensure there are event listeners added for
            // the events we expect.  This shoudl be doen before we make the request expecting
            // the events or else we may miss events.
            setupEventListeners(connectedInstance);

            // We must next say hello
            sayHello(connectedInstance);

        }

    }

    private static void setupEventListeners(final ClientContainer.ConnectedInstance connectedInstance) {
        //TODO Set up
    }

    private static void sayHello(final ClientContainer.ConnectedInstance connectedInstance) {

        final HelloRequest helloRequestPayload = new HelloRequest();
        helloRequestPayload.setName("Johnny!");

        final SimpleRequest helloRequest = SimpleRequest.builder()
                .payload(helloRequestPayload)
            .build();

        final Response helloResponse = connectedInstance.getRealiable().sendRequest(helloRequest, HelloResponse.class);

        if (helloResponse.getResponseHeader().getCode() != ResponseCode.OK.getCode()) {
            throw new RuntimeException("Got bad response " + helloResponse);
        }

        final HelloResponse helloResponsePayload = helloResponse.getPayload(HelloResponse.class);

        LOG.info("Got response {} {}", helloResponsePayload.getMessage(), helloResponsePayload.getDetails());

    }

}
