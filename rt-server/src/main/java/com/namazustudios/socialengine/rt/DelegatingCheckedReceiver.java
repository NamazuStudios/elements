package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.edge.SimpleEdgeRequestDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Essentially, this checks for two conditions.  First, it ensures that only
 * a single response is sent to the client.  In the event the request does
 * not generate a response, a null response is generated with an instance of
 * {@link ResponseCode#OK}.
 *
 * This uses an instance of {@link AtomicBoolean} to ensure that the response
 * is generated only once.
 *
 */
public class DelegatingCheckedReceiver implements ResponseReceiver, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatingCheckedReceiver.class);

    private final Request request;

    private final ResponseReceiver delegate;

    private final AtomicBoolean received = new AtomicBoolean();

    public DelegatingCheckedReceiver(final Request request,
                                     final ResponseReceiver delegate) {
        this.request = request;
        this.delegate = delegate;
    }

    @Override
    public void receive(int code, Object payload) {
        if (received.compareAndSet(false, true)) {
            delegate.receive(code, payload);
        } else {
            LOG.error("Attempted to dispatch duplicate responses for request {}", request);
        }
    }

    @Override
    public void close()  {
        if (received.compareAndSet(false, true)) {

            final String msg = "EdgeRequestDispatcher failed to generate response.";

            final SimpleExceptionResponsePayload simpleExceptionResponsePayload;
            simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();
            simpleExceptionResponsePayload.setMessage(msg);

            delegate.receive(ResponseCode.INTERNAL_ERROR_FATAL.getCode(), simpleExceptionResponsePayload);

        }
    }

}
