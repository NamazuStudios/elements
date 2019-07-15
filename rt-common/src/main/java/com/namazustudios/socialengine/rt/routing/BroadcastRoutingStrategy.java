package com.namazustudios.socialengine.rt.routing;

import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * This routing stragegy will broadcast to all nodes.  It performs no aggregation and only works with methods returning
 * void or accepting a Void response.  More specifically, the return must always be null for all consumers.
 */
public class BroadcastRoutingStrategy extends AbstractCombiningRoutingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastRoutingStrategy.class);

    @Override
    protected InvocationResult newInitialInvocationResult() {
        final InvocationResult result = new InvocationResult();
        return result;
    }

    @Override
    protected InvocationResult combine(final InvocationResult ra, final InvocationResult rb) {
        final Object a = ra.getResult();
        final Object b = rb.getResult();
        ra.setResult(combine(a, b));
        return ra;
    }

    @Override
    protected Object combine(final Object a, final Object b) {
        if (a != null || b != null) logger.warn("Trying to combine {} and {}.  Translating to null.", a, b);
        return null;
    }

}
