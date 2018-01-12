package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.MultiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * Contains all {@link Node} instances for several {@Link Node} instances and manages their life cycles therein.  This
 * imposes the additional requirement of providing some form of {@link ConnectionDemultiplexer} to route internal
 * requests.
 */
public class MultiNodeContainer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MultiNodeContainer.class);

    private Set<Node> nodeSet;

    private ConnectionDemultiplexer connectionDemultiplexer;

    /**
     * Attempts to start each {@link Node}, throwing an instance of {@link MultiException} if any {@link Node} fails
     * to startup.
     */
    public void start() {

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            getConnectionDemultiplexer().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
        }

        exceptionList.addAll(getNodeSet().parallelStream().map(node -> {
            try {
                node.start();
                return null;
            } catch (Exception ex) {
                logger.error("Error starting node.", ex);
                return ex;
            }
        }).filter(ex -> ex != null).collect(toList()));

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    /**
     * Attempts to stop each {@link Node}, throwing an instance of {@link MultiException} if
     */
    @Override
    public void close() {

        final List<Exception> exceptionList = getNodeSet().parallelStream().map(node -> {
            try {
                node.stop();
                return null;
            } catch (Exception ex) {
                logger.error("Error starting node.", ex);
                return ex;
            }
        }).filter(ex -> ex != null).collect(toList());

        try {
            getConnectionDemultiplexer().stop();
        } catch (Exception ex) {
            exceptionList.add(ex);
        }

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    public Set<Node> getNodeSet() {
        return nodeSet;
    }

    @Inject
    public void setNodeSet(Set<Node> nodeSet) {
        this.nodeSet = nodeSet;
    }

    public ConnectionDemultiplexer getConnectionDemultiplexer() {
        return connectionDemultiplexer;
    }

    @Inject
    public void setConnectionDemultiplexer(ConnectionDemultiplexer connectionDemultiplexer) {
        this.connectionDemultiplexer = connectionDemultiplexer;
    }

}
