package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.remote.MessageType.INVOCATION_ERROR;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class JeroMQNodeInvocation {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQNodeInvocation.class);

    private final LocalInvocationDispatcher localInvocationDispatcher;

    private final PayloadWriter payloadWriter;

    private final PayloadReader payloadReader;

    private final AsyncConnectionPool<ZContext, ZMQ.Socket> outbound;

    private final ZMsg identity;

    private final AtomicBoolean sync = new AtomicBoolean();

    private final AtomicInteger remaining;

    private final Consumer<InvocationResult> syncInvocationResultConsumer;

    private final Consumer<InvocationError> syncInvocationErrorConsumer;

    private final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList;

    private final Consumer<InvocationError> asyncInvocationErrorConsumer;

    private final byte[] payload;

    public JeroMQNodeInvocation(final ZMsg incoming,
                                final LocalInvocationDispatcher localInvocationDispatcher,
                                final PayloadReader payloadReader,
                                final PayloadWriter payloadWriter,
                                final AsyncConnectionPool<ZContext, ZMQ.Socket> outbound) {

        this.localInvocationDispatcher = localInvocationDispatcher;
        this.payloadWriter = payloadWriter;
        this.payloadReader = payloadReader;
        this.outbound = outbound;
        this.identity = popIdentity(incoming);

        final RequestHeader requestHeader = new RequestHeader();
        requestHeader.getByteBuffer().put(incoming.remove().getData());

        final int remaining = requestHeader.additionalParts.get();
        this.remaining = new AtomicInteger(remaining);

        payload = incoming.remove().getData();

        this.syncInvocationResultConsumer = r -> {
            if (sync.getAndSet(true)) {
                logger.error("Already set sync response.  Ignoring {}", r);
            } else {
                sendResult(r, 0);
            }
        };

        this.syncInvocationErrorConsumer = e -> {
            if (this.sync.getAndSet(true)) {
                logger.error("Already set sync response.  Ignoring {}", e);
            } else {
                sendError(e, 0);
            }
        };

        this.asyncInvocationErrorConsumer = e -> {
            if (this.remaining.getAndSet(0) <= 0) {
                logger.error("Suppressing invocation error.  Already sent.", e.getThrowable());
            } else {
                sendError(e, 1);
            }
        };

        this.asyncInvocationResultConsumerList = range(0, remaining)
            .map(index -> index + 1)
            .mapToObj(part -> (Consumer<InvocationResult>) r -> {
                if (this.remaining.getAndDecrement() <= 0) {
                    logger.debug("Ignoring invocation result {} because of previous errors.", r);
                } else {
                    sendResult(r, part);
                }
        }).collect(toList());
    }

    public void dispatch() {

        final Invocation invocation;

        try {
            invocation = payloadReader.read(Invocation.class, payload);
        } catch (final IOException ex) {
            final InvocationError invocationError = new InvocationError();
            invocationError.setThrowable(ex);
            sendError(invocationError, 0);
            return;
        }

        localInvocationDispatcher.dispatch(
            invocation,
            syncInvocationResultConsumer, syncInvocationErrorConsumer,
            asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);

        if (!sync.get()) {
            throw new InternalException("Sync callback was not made.");
        }

    }

    private void sendResult(final InvocationResult invocationResult, final int part) {

        final ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.type.set(MessageType.INVOCATION_RESULT);
        responseHeader.part.set(part);

        final byte[] responseHeaderBytes = new byte[responseHeader.size()];
        responseHeader.getByteBuffer().get(responseHeaderBytes);

        final byte[] payload;

        try {
            payload = payloadWriter.write(invocationResult);
        } catch (IOException e) {
            logger.error("Could not write payload to byte stream.  Sending empty.", e);
            final InvocationError invocationError = new InvocationError();
            invocationError.setThrowable(e);
            sendError(invocationError, part);
            return;
        }

        final ZMsg msg = identity.duplicate();

        msg.addLast(EMPTY_DELIMITER);
        msg.addLast(responseHeaderBytes);
        msg.addLast(payload);

        write(msg);

    }

    private void sendError(final InvocationError invocationError, final int part) {

        final ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.type.set(INVOCATION_ERROR);
        responseHeader.part.set(part);

        final byte[] responseHeaderBytes = new byte[responseHeader.size()];
        responseHeader.getByteBuffer().get(responseHeaderBytes);

        byte[] payload;

        try {
            payload = payloadWriter.write(invocationError);
        } catch (Exception e) {
            logger.error("Could not write payload to byte stream.  Sending empty payload.", e);
            payload = new byte[0];
        }

        final ZMsg msg = identity.duplicate();

        msg.addLast(EMPTY_DELIMITER);
        msg.addLast(responseHeaderBytes);
        msg.addLast(payload);

        write(msg);

    }

    private void write(final ZMsg msg) {
        outbound.acquireNextAvailableConnection(c -> c.onWrite(c0 -> {
            msg.send(c.socket());
            c.recycle();
        }));
    }

}
