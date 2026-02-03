package dev.getelements.elements.jetty;

import dev.getelements.elements.common.app.ApplicationDeploymentService;
import dev.getelements.elements.common.app.ElementRuntimeService;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.sdk.model.exception.InternalException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.common.app.ApplicationDeploymentService.APP_SERVE;

public class ElementsWebServices implements Runnable {

    public static final String MAIN_HANDLER = "dev.getelements.elements.jetty.handler.main";
    private static final Logger logger = LoggerFactory.getLogger(ElementsWebServices.class);

    private Server server;

    private Instance instance;

    private ApplicationDeploymentService appServeApplicationDeploymentService;

    private ElementRuntimeService elementRuntimeService;

    public void start() {

        getInstance().start();

        try {
            getServer().start();
        } catch (Exception ex) {
            throw new InternalException("Could not deployAvailableApplications Jetty server.", ex);
        }

        getAppServeApplicationDeploymentService().deployAvailableApplications();

        getElementRuntimeService().start();

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

    public ApplicationDeploymentService getAppServeApplicationDeploymentService() {
        return appServeApplicationDeploymentService;
    }

    @Inject
    public void setAppServeApplicationDeploymentService(@Named(APP_SERVE) ApplicationDeploymentService appServeApplicationDeploymentService) {
        this.appServeApplicationDeploymentService = appServeApplicationDeploymentService;
    }

    public ElementRuntimeService getElementRuntimeService() {
        return elementRuntimeService;
    }

    @Inject
    public void setElementRuntimeService(ElementRuntimeService elementRuntimeService) {
        this.elementRuntimeService = elementRuntimeService;
    }

}
