package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.exception.MultiException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Contains all {@link Node} instances for each {@link Application} and manages their lifecycles therein.
 */
public class ApplicationNodeContainer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNodeContainer.class);

    private Map<String, Node> applicationNodes;

    /**
     * Attempts to start each {@link Node}, throwing an instance of {@link MultiException} if any {@link Node} fails
     * to startup.
     */
    public void start() {

        final List<Exception> exceptionList = getApplicationNodes().values().parallelStream().map(node -> {
            try {
                node.start();
                return null;
            } catch (Exception ex) {
                logger.error("Error starting node.", ex);
                return ex;
            }
        }).filter(ex -> ex != null).collect(toList());

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    /**
     * Attempts to stop each {@link Node}, throwing an instance of {@link MultiException} if
     */
    @Override
    public void close() {

        final List<Exception> exceptionList = getApplicationNodes().values().parallelStream().map(node -> {
            try {
                node.stop();
                return null;
            } catch (Exception ex) {
                logger.error("Error starting node.", ex);
                return ex;
            }
        }).filter(ex -> ex != null).collect(toList());

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }
    }

    public Map<String, Node> getApplicationNodes() {
        return applicationNodes;
    }

    @Inject
    public void setApplicationNodes(Map<String, Node> applicationNodes) {
        this.applicationNodes = applicationNodes;
    }

}
