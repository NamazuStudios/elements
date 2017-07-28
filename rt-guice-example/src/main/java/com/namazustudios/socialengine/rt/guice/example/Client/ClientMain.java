package com.namazustudios.socialengine.rt.guice.example.client;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.mina.guice.MinaDefaultClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.List;

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

        try (final ClientContainer.ConnectedInstance connectedInstance = clientContainer.connect(inetSocketAddress);
             final BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {

            // We must first say hello to the server.
            sayHello(input, connectedInstance);

            // Obtain the clock we want to subscribe
            final ClockMetadata clockMetadata = listClocks(input, connectedInstance);

            // Send the request to actually subscribe
            subscribe(clockMetadata, connectedInstance);

            // And finally subscribe, then wait for the user to enter "bye" into the terminal
            final List<Observation> observationList = setupLocalObservers(clockMetadata, connectedInstance);
            waitForTermination(input);

            // Remove all observations fromt he server.
            for (final Observation observation : observationList) {
                observation.release();
            }

        }

    }

    private static final String readLine(final BufferedReader input) {
        do {
            try {

                final String line = input.readLine().trim();

                if (line.length() > 0) {
                    return line;
                }

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } while (true);
    }

    private static void sayHello(final BufferedReader input,
                                 final ClientContainer.ConnectedInstance connectedInstance) {

        LOG.info("Enter your name.");

        final HelloRequest helloRequestPayload = new HelloRequest();
        helloRequestPayload.setName(readLine(input));

        final SimpleRequest helloRequest = SimpleRequest.builder()
                .path("/hello")
                .method("introduce_yourself")
                .payload(helloRequestPayload)
            .build();

        final Response helloResponse = connectedInstance.getRealiable().sendRequest(helloRequest, HelloResponse.class);

        if (helloResponse.getResponseHeader().getCode() != ResponseCode.OK.getCode()) {
            throw new RuntimeException("Got bad response " + helloResponse);
        }

        final HelloResponse helloResponsePayload = helloResponse.getPayload(HelloResponse.class);
        LOG.info("Got response {} {}", helloResponsePayload.getMessage(), helloResponsePayload.getDetails());

    }

    private static ClockMetadata listClocks(final BufferedReader input,
                                            final ClientContainer.ConnectedInstance connectedInstance) {

        LOG.info("Enter your name.");

        final SimpleRequest helloRequest = SimpleRequest.builder()
                .path("/clocks")
                .method("list_clocks")
            .build();

        final Response helloResponse = connectedInstance.getRealiable().sendRequest(helloRequest, ListClocksResponse.class);

        if (helloResponse.getResponseHeader().getCode() != ResponseCode.OK.getCode()) {
            throw new RuntimeException("Got bad response " + helloResponse);
        }

        final ListClocksResponse listClocksResponse = helloResponse.getPayload(ListClocksResponse.class);

        ClockMetadata metadata;

        do {

            for (final ClockMetadata clockMetadata : listClocksResponse.getClocks()) {
                LOG.info("Clock {} with timezone {} at location {} ", clockMetadata.getName(),
                                                                      clockMetadata.getTimeZone(),
                                                                      clockMetadata.getLocation());
            }

            LOG.info("Enter the clock's ID");

            final String clockId = readLine(input);

            metadata = Iterables.find(listClocksResponse.getClocks(), new Predicate<ClockMetadata>() {

                @Override
                public boolean apply(ClockMetadata input) {
                    return clockId.equals(input.getName());
                }

            });

        } while (metadata == null);

        return metadata;

    }

    private static void subscribe(final ClockMetadata clockMetadata,
                                  final ClientContainer.ConnectedInstance connectedInstance) {

        final ClockSubscriptionRequest clockSubscriptionRequest = new ClockSubscriptionRequest();
        clockSubscriptionRequest.setName(clockMetadata.getName());

        final SimpleRequest helloRequest = SimpleRequest.builder()
                .path("/clocks")
                .method("subscribe")
                .payload(clockSubscriptionRequest)
            .build();

        connectedInstance.getRealiable().sendRequest(helloRequest, Void.class);

    }

    private static List<Observation> setupLocalObservers(final ClockMetadata clockMetadata,
                                                         final ClientContainer.ConnectedInstance connectedInstance) {

        final Path path = new Path(clockMetadata.getPath());
        LOG.info("Adding observer for events at path {}", path);

        final EventReceiver<ClockTimeEvent> eventReceiver = new EventReceiver<ClockTimeEvent>() {

            @Override
            public Class<ClockTimeEvent> getEventType() {
                return ClockTimeEvent.class;
            }

            @Override
            public void receive(Event event) {

                final ClockTimeEvent clockTimeEvent = event.getPayload(ClockTimeEvent.class);

                LOG.info("Event: {} Path: {} Time: {}", event.getEventHeader().getName(),
                        event.getEventHeader().getPath(),
                        clockTimeEvent.getTime());

            }

        };

        return Lists.newArrayList(
            connectedInstance.getRealiable().observe(path, "tick tock", eventReceiver),
            connectedInstance.getRealiable().observe(path, "ding dong", eventReceiver));

    }

    private static void waitForTermination(final BufferedReader input) {

        String line;

        do {
            line = readLine(input);
        } while (!"bye".equals(line));

    }

}
