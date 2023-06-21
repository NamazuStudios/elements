package dev.getelements.elements.jetty;

import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.rt.remote.Instance;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ElementsWebServices implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ElementsWebServices.class);

    private Server server;

    private Instance instance;

    public void start() {

        getInstance().start();

        try {
            getServer().start();
        } catch (Exception ex) {
            throw new InternalException("Could not start Jetty server.", ex);
        }

    }

    public void run() {
        try {
            server.join();
        } catch (InterruptedException ex) {
            throw new InternalException("Interrupted running Jetty server.", ex);
        }
    }

    public void stop() {

        try {
            server.stop();
        } catch (Exception ex) {
            logger.error("Caught exception shutting down server.", ex);
        }

        try {
            instance.close();
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

}
