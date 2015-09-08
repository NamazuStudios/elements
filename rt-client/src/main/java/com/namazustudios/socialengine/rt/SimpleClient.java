package com.namazustudios.socialengine.rt;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the simple client implementation.  This client actually has no knowledge of
 * how the underlying transport operates, but deals strictly with tracking request/response
 * pairs, managing instances of {@link EventReceiver} instances.
 *
 * Created by patricktwohig on 9/4/15.
 */
public class SimpleClient implements Client, Client.NetworkOperations {

    /**
     * The number of pending requests that are allowed to be happening at the same time before
     * this client will automatically assume the request will not be handled and it will be culled.
     */
    public static final String MAX_PENDING_REQUESTS =
        "com.namazustudios.socialengine.rt.SimpleClient.MAX_PENDING_REQUESTS";

    private static final Logger LOG = LoggerFactory.getLogger(SimpleClient.class);

    private final AtomicInteger requestSequence = new AtomicInteger();

    private final ConcurrentNavigableMap<Integer, SimpleClientPendingRequest> pendingRequests = new ConcurrentSkipListMap<>();

    private final ClientRequestDispatcher clientRequestDispatcher;

    @Inject
    private ClientEventReceiverMap clientEventReceiverMap;

    @Inject
    @Named(MAX_PENDING_REQUESTS)
    private int maxPendingRequests;

    public SimpleClient(ClientRequestDispatcher clientRequestDispatcher) {
        this.clientRequestDispatcher = clientRequestDispatcher;
    }

    @Override
    public PendingRequest sendRequest(final Request request,
                                      final Class<?> responseType,
                                      final ResponseReceiver receiver) {

        if (pendingRequests.size() >= maxPendingRequests) {
            throw new TooBusyException(
                    "Pending requests >=" + maxPendingRequests + " " +
                    "(You are trying to make too many concurrent parallel requests.)");
        }

        final int sequence = requestSequence.getAndIncrement();

        final SimpleRequest simpleRequest = SimpleRequest.builder()
                .from(request)
                .sequence(sequence)
            .build();

        final SimpleClientPendingRequest simpleClientPendingRequest =
            new SimpleClientPendingRequest(responseType, receiver);

        pendingRequests.put(sequence, simpleClientPendingRequest);
        clientRequestDispatcher.dispatch(simpleRequest);
        LOG.trace("Sending request {} with sequence {}", request, sequence);

        return new PendingRequest() {

            @Override
            public void cancel() {
                cancel(false);
            }

            @Override
            public void cancel(boolean silent) {

                final SimpleClientPendingRequest tuple = pendingRequests.remove(sequence);

                if (tuple != null && !silent) {
                    final SimpleResponse simpleResponse = SimpleResponse.builder()
                            .code(ResponseCode.USER_CANCELED_FATAL)
                            .build();
                    tuple.responseReceiver.receive(simpleResponse);
                }

            }

        };

    }

    @Override
    public <EventT> Subscription subscribe(final Path path,
                                           final String name,
                                           final EventReceiver<EventT> eventReceiver) {
        return null;
    }

    @Override
    public Class<?> getPayloadType(final ResponseHeader responseHeader) {

        final SimpleClientPendingRequest pendingRequest = pendingRequests.get(responseHeader.getSequence());

        if (pendingRequest == null) {
            throw new NotFoundException();
        }

        return pendingRequest.responseType;

    }

    @Override
    public void receive(final Response response) {

        // We remove it here to ensure that no further action can be taken against
        // this particular request tuple

        final SimpleClientPendingRequest simpleClientPendingRequest =
                pendingRequests.remove(response.getResponseHeader().getSequence());

        LOG.trace("Receved response {}", response);

        if (simpleClientPendingRequest == null) {
            // Just log it for now.  This isn't necessairly an error because this could
            // happen in spite of the fact that the request was cancelled.  In which case
            // we just dispose of the response.
            LOG.info("Recieved reponse with no matching request {}.", response);
        } else {
            simpleClientPendingRequest.responseReceiver.receive(response);
        }

    }

    @Override
    public Iterable<Class<?>> getEventTypes(final EventHeader eventHeader) {
        final Path path = new Path(eventHeader.getPath());
        final String name = eventHeader.getName();
        final Iterable<? extends  EventReceiver<?>> eventReceivers = clientEventReceiverMap.getEventReceivers(path, name);
        return Iterables.transform(eventReceivers, new Function<EventReceiver<?>, Class<?>>() {
            @Override
            public Class<?> apply(EventReceiver<?> input) {
                return input.getEventType();
            }

        });
    }

    @Override
    public void receive(final Event event, final Class<?> eventType) {

        final Path path = new Path(event.getEventHeader().getPath());
        final String name = event.getEventHeader().getName();

        for(EventReceiver<?> eventReceiver : clientEventReceiverMap.getEventReceivers(path, name)) {
            try {
                final Object payload = eventReceiver.getEventType().cast(event.getPayload());
                final EventReceiver<Object> objectEventReceiver = (EventReceiver<Object>)eventReceiver;
                objectEventReceiver.receive(path, name, event.getPayload());
            } catch (ClassCastException ex) {
                LOG.error("Caught exception trying to process event {} with receiver {}.", event, eventReceiver);
            }
        }

    }

    private class SimpleClientPendingRequest {

        private final Class<?> responseType;

        private final ResponseReceiver responseReceiver;

        public SimpleClientPendingRequest(final Class<?> responseType,
                                          final ResponseReceiver responseReceiver) {

            if (responseType == null) {
                throw new IllegalArgumentException("responseType must be non-null");
            } else if (responseReceiver == null) {
                throw new IllegalArgumentException("responseReceiver must be non-null");
            }

            this.responseType = responseType;
            this.responseReceiver = responseReceiver;

        }

    }

}
