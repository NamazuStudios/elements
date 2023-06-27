package dev.getelements.elements.rt.routing;

import dev.getelements.elements.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This routing stragegy will broadcast to all nodes.  It performs no aggregation and only works with methods returning
 * void or accepting a Void response.  More specifically, the return must always be null for all consumers.
 */
public class BroadcastRoutingStrategy extends AbstractAggregateRoutingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastRoutingStrategy.class);

    @Override
    protected Object newInitialResult() {
        return null;
    }

    @Override
    protected Object combine(final Object a, final Object b) {
        if (a != null || b != null) logger.warn("Trying to combine {} and {}.  Translating to null.", a, b);
        return null;
    }

}
