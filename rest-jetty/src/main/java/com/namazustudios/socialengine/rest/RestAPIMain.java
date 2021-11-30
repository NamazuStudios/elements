package com.namazustudios.socialengine.rest;

import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.rest.guice.RestAPIModule;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.servlet.security.HappyServlet;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.Stage.DEVELOPMENT;
import static com.namazustudios.socialengine.rest.guice.GuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static java.util.EnumSet.allOf;
import static org.eclipse.jetty.util.Loader.getResource;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class RestAPIMain implements Callable<Void>, Runnable {

    private static final Logger logger = getLogger(RestAPIMain.class);

    private static final OptionParser optionParser = new OptionParser();

    public static final int DEFAULT_PORT = 8081;

    public static final Stage DEFAULT_STAGE = DEVELOPMENT;

    public static final String DEFAULT_API_CONTEXT = "api";

    private static final OptionSpec<Stage> stageOptionSpec = optionParser
        .accepts("stage", "Is this running in development or production?")
        .withOptionalArg()
        .ofType(Stage.class)
        .defaultsTo(DEFAULT_STAGE);

    private static final OptionSpec<Void> helpOptionSpec = optionParser
        .accepts("help", "Displays the help message.")
        .forHelp();

    private final Server server;

    private final Instance instance;

    /**
     * Args style constructor.
     *
     * @param args the command-line arguments
     * @throws ProgramArgumentException if there was a problem parsing the command line arguments
     */
    public RestAPIMain(final String[] args) throws ProgramArgumentException {

        Stage stage;

        try {

            final OptionSet options = optionParser.parse(args);

            if (options.hasArgument(helpOptionSpec)) {
                throw new HelpRequestedException();
            }

            stage = options.valueOf(stageOptionSpec);

        } catch (OptionException ex) {
            throw new ProgramArgumentException(ex);
        }

        final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final var injector = createInjector(stage,
            new RestAPIServerModule(),
            new RestAPIModule(defaultConfigurationSupplier));

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var restDocRedirectFilter = injector.getInstance(RestDocRedirectFilter.class);
        final var servletHandler = injector.getInstance(ServletContextHandler.class);

        this.server = injector.getInstance(Server.class);
        this.instance = injector.getInstance(Instance.class);
        doInit(injector, guiceFilter, restDocRedirectFilter, servletHandler);

    }

    @Inject
    private RestAPIMain(final Server server,
                        final Instance instance,
                        final Injector injector,
                        final GuiceFilter guiceFilter,
                        final RestDocRedirectFilter restDocRedirectFilter,
                        final ServletContextHandler servletHandler) {
        this.server = server;
        this.instance = instance;
        doInit(injector, guiceFilter, restDocRedirectFilter, servletHandler);
    }

    private void doInit(final Injector injector,
                        final GuiceFilter guiceFilter,
                        final RestDocRedirectFilter restDocRedirectFilter,
                        final ServletContextHandler servletHandler) {

        final var collection = new HandlerCollection();

        servletHandler.getServletContext().setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);
        servletHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletHandler.addFilter(new FilterHolder(restDocRedirectFilter), "/*", allOf(DispatcherType.class));

        collection.addHandler(servletHandler);

        if (!"/".equals(servletHandler.getContextPath())) {
            final var rootServletHandler = new ServletContextHandler();
            rootServletHandler.setContextPath("/");
            rootServletHandler.addServlet(HappyServlet.class, "/");
            collection.addHandler(rootServletHandler);
        }

        server.setHandler(collection);

    }

    /**
     * Starts the server, and returns immediately.
     *
     * @throws Exception
     */
    public void start() throws Exception {
        instance.start();
        server.start();
    }

    /**
     * Stops the server and waits for it to complete.
     *
     * @throws Exception
     */
    public void stop() throws Exception {
        server.stop();
        server.join();
        instance.close();
    }

    /**
     * Runs this RestAPI and blocks until the server exist.
     *
     * @throws Exception if there is an exception running the server.
     */
    @Override
    public void run() throws ServerRuntimeException {
        try {
            instance.start();
            server.start();
            server.dumpStdErr();
            server.join();
        } catch (RuntimeException ex){
            throw ex;
        } catch (Exception ex) {
            throw new ServerRuntimeException(ex);
        }
    }

    @Override
    public Void call() throws Exception {
        server.start();
        server.dumpStdErr();
        server.join();
        instance.close();
        return null;
    }

    /**
     * Gets the {@link Instance} used by this {@link RestAPIMain}.
     *
     * @return the {@link Instance}
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * Thrown by the {@link RestAPIMain#run()} method.  The value of {@link #getCause()} will always be the exact
     * cause of the underlying exception.
     */
    public static class ServerRuntimeException extends RuntimeException {
        public ServerRuntimeException(final Throwable ex) {
            super(ex);
        }
    }

    /**
     * Thrown when bad arguments are passed to the {@link RestAPIMain#RestAPIMain(String[])} constructor or the
     * arguments supplied cannot be used to creat the server.
     */
    public static class ProgramArgumentException extends IllegalArgumentException {

        public ProgramArgumentException() {}

        public ProgramArgumentException(final OptionException ex) {
            super(ex.getMessage(), ex);
        }

    }

    /**
     * Thrown when the options specified a request for help.
     */
    public static class HelpRequestedException extends ProgramArgumentException {}

    public static void main(final String[] args) throws Exception {
        try {
            final RestAPIMain main = new RestAPIMain(args);
            main.run();
        } catch (final ProgramArgumentException ex) {
            logger.debug("Bad program arguments.", ex);
            optionParser.printHelpOn(System.out);
        }
    }

}
