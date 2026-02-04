package dev.getelements.elements.jetty;

import dev.getelements.elements.common.app.ElementContainerService;
import dev.getelements.elements.common.app.ElementRuntimeService;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.sdk.model.exception.InternalException;
import jakarta.inject.Inject;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementsWebServices implements Runnable {

    public static final String MAIN_HANDLER = "dev.getelements.elements.jetty.handler.main";
    private static final Logger logger = LoggerFactory.getLogger(ElementsWebServices.class);

    private Server server;

    private Instance instance;

    private ElementRuntimeService elementRuntimeService;

    private ElementContainerService elementContainerService;

    public void start() {

        getInstance().start();

        try {
            getServer().start();
        } catch (Exception ex) {
            throw new InternalException("Could not deployAvailableApplications Jetty server.", ex);
        }

        getElementRuntimeService().start();
        getElementContainerService().start();

    }

    public void run() {
        try {
            getServer().join();
        } catch (InterruptedException ex) {
            throw new InternalException("Interrupted running Jetty server.", ex);
        }
    }

    public void stop() {

        try {
            getElementContainerService().stop();
        } catch (Exception ex) {
            logger.error("Caught exception stopping ElementRuntimeService.", ex);
        }

        try {
            getElementRuntimeService().stop();
        } catch (Exception ex) {
            logger.error("Caught exception stopping ElementRuntimeService.", ex);
        }

        try {
            getServer().stop();
        } catch (Exception ex) {
            logger.error("Caught exception shutting down server.", ex);
        }

        try {
            getInstance().close();
        } catch (Exception ex) {
            logger.error("Caught exception stopping Elements instance.", ex);
        }

    }

    public Server getServer() {
        return server;
    }

    @Inject
    public void setServer(Server server) {
        this.server = server;
    }

    public Instance getInstance() {
        return instance;
    }

    @Inject
    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public ElementRuntimeService getElementRuntimeService() {
        return elementRuntimeService;
    }

    @Inject
    public void setElementRuntimeService(ElementRuntimeService elementRuntimeService) {
        this.elementRuntimeService = elementRuntimeService;
    }

    public ElementContainerService getElementContainerService() {
        return elementContainerService;
    }

    @Inject
    public void setElementContainerService(ElementContainerService elementContainerService) {
        this.elementContainerService = elementContainerService;
    }

}
