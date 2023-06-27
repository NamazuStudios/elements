package dev.getelements.elements.rt;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.exception.TooBusyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * This is the simple client implementation.  This client actually has no knowledge of
 * how the underlying transport operates, but deals strictly with tracking request/response
 * pairs, managing instances of {@link EventReceiver} instances.
 *
 * Created by patricktwohig on 9/4/15.
 */
public class DefaultClient implements Client, IncomingNetworkOperations  {

    /**
     * The number of pending requests that are allowed to be happening at the same time before
     * this client will automatically assume the request will not be handled and it will be culled.
     */
    public static final String MAX_PENDING_REQUESTS = "dev.getelements.elements.rt.DefaultClient.MAX_PENDING_REQUESTS";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultClient.class);

    private final AtomicInteger requestSequence = new AtomicInteger();

    private final ConcurrentNavigableMap<Integer, SimpleClientPendingRequest> pendingRequests = new ConcurrentSkipListMap<>();

    private final int maxPendingRequests;

    private final EventServiceOld eventService;

    private final OutgoingNetworkOperations outgoingNetworkOperations;

    @Inject
    public DefaultClient(@Named(MAX_PENDING_REQUESTS) final int maxPendingRequests,
                         final EventServiceOld eventService,
                         final OutgoingNetworkOperations outgoingNetworkOperations) {
        this.maxPendingRequests = maxPendingRequests;
        this.eventService = eventService;
        this.outgoingNetworkOperations = outgoingNetworkOperations;
    }

    @Override
    public Response sendRequest(Request request, Class<?> expectedType) {

        final AtomicReference<Response> responseAtomicReference = new AtomicReference<>();

        sendRequest(request, expectedType, response -> {
            synchronized (responseAtomicReference) {
                responseAtomicReference.set(response);
                responseAtomicReference.notifyAll();
            }
        });

        synchronized (responseAtomicReference) {
            try {
                while (responseAtomicReference.get() == null) {
                    responseAtomicReference.wait();
                }
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }
        }

        return responseAtomicReference.get();

    }

    @Override
    public PendingRequest sendRequest(final Request request,
                                      final Class<?> responseType,
                                      final Consumer<Response> responseConsumer) {

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

        final SimpleClientPendingRequest simpleClientPendingRequest;
        simpleClientPendingRequest = new SimpleClientPendingRequest(responseType, responseConsumer);

        pendingRequests.put(sequence, simpleClientPendingRequest);
        outgoingNetworkOperations.dispatch(simpleRequest);
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
                    tuple.responseConsumer.accept(simpleResponse);
                }

            }

        };

    }

    @Override
    public <EventT> Observation observe(final Path path,
                                        final String name,
                                        final EventReceiver<EventT> eventReceiver) {
        return eventService.observe(path, name, eventReceiver);
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
            simpleClientPendingRequest.responseConsumer.accept(response);
        }

    }

    @Override
    public Iterable<Class<?>> getEventTypes(final EventHeader eventHeader) {

        final Path path = new Path(eventHeader.getPath());
        final String name = eventHeader.getName();
        final Iterable<? extends  EventReceiver<?>> eventReceivers = eventService.getEventReceivers(path, name);

        return Iterables.transform(eventReceivers, (Function<EventReceiver<?>, Class<?>>) input -> input.getEventType());

    }

    @Override
    public void receive(final Event event) {
        eventService.post(event);
    }

    private class SimpleClientPendingRequest {

        private final Class<?> responseType;

        private final Consumer<Response> responseConsumer;

        public SimpleClientPendingRequest(final Class<?> responseType,
                                          final Consumer<Response> responseConsumer) {

            if (responseType == null) {
                throw new IllegalArgumentException("responseType must be non-null");
            } else if (responseConsumer == null) {
                throw new IllegalArgumentException("responseReceiver must be non-null");
            }

            this.responseType = responseType;
            this.responseConsumer = responseConsumer;

        }

    }

}
