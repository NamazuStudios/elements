package com.namazustudios.socialengine.rest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.rest.guice.RestAPIModule;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;

import javax.servlet.DispatcherType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.namazustudios.socialengine.rest.guice.GuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static java.util.EnumSet.allOf;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;
import static org.eclipse.jetty.util.Loader.getResource;
import static org.eclipse.jetty.util.resource.Resource.newClassPathResource;
import static org.slf4j.LoggerFactory.getLogger;

public class RestAPIMain implements Callable<Void>, Runnable {

    private static final Logger logger = getLogger(RestAPIMain.class);

    private static final OptionParser optionParser = new OptionParser();

    private static final OptionSpec<String> bindOptionSpec = optionParser
            .accepts("bind", "The bind address.")
            .withOptionalArg()
            .ofType(String.class)
            .defaultsTo("0.0.0.0");

    private static final OptionSpec<Integer> portOptionSpec = optionParser
            .accepts("port", "The TCP Port upon which to bind.")
            .withOptionalArg()
            .ofType(Integer.class)
            .defaultsTo(8080);

    private static final OptionSpec<String> apiContextOptionsSpec = optionParser
            .accepts("api-context", "The context upon which to run the api.")
            .withOptionalArg()
            .ofType(String.class)
            .defaultsTo("/api");

    private static final OptionSpec<Void> helpOptionSpec = optionParser
            .accepts("help", "Displays the help message.")
            .forHelp();

    private final Server server = new Server();

    public RestAPIMain(final String[] args) throws ProgramArgumentException {

        int port;
        String bind;
        String apiContext;

        try {

            final OptionSet options = optionParser.parse(args);

            if (options.hasArgument(helpOptionSpec)) {
                throw new HelpRequestedException();
            }

            bind = options.valueOf(bindOptionSpec);
            port = options.valueOf(portOptionSpec);
            apiContext = options.valueOf(apiContextOptionsSpec);
            apiContext = apiContext.startsWith("/") ? apiContext : "/" + apiContext;

        } catch (OptionException ex) {
            throw new ProgramArgumentException(ex);
        }

        init(port, bind, apiContext);

    }

    public RestAPIMain(final int port, final String bind,
                       final String apiContext) {
        init(port, bind, apiContext);
    }

    private void init(final int port, final String bind, final String apiContext) {

        final ServerConnector connector = new ServerConnector(server);
        connector.setHost(bind);
        connector.setPort(port);

        final HandlerCollection handlerCollection = new HandlerCollection();

        final ServletContextHandler servletHandler = new ServletContextHandler(SESSIONS);
        servletHandler.setContextPath(apiContext);

        final Injector injector = Guice.createInjector(new RestAPIModule());
        servletHandler.getServletContext().setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);

        final GuiceFilter guiceFilter = injector.getInstance(GuiceFilter.class);
        servletHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));

        final Map<String, String> defaultInitParameters = new HashMap<>();
        defaultInitParameters.put("dirAllowed", "false");
        defaultInitParameters.put("resourceBase", getResource("swagger").toString());

        final ServletHolder defaultServletHolder = servletHandler.addServlet(DefaultServlet.class, "/");
        defaultServletHolder.setInitParameters(defaultInitParameters);

        handlerCollection.addHandler(servletHandler);

        server.setHandler(handlerCollection);
        server.setConnectors(new Connector[]{connector});

    }

    /**
     * Runs this RestAPI and blocks until the server exist.
     *
     * @throws Exception if there is an exception running the server.
     */
    @Override
    public void run() throws ServerRuntimeException {
        try {
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
        return null;
    }

    /**
     * Thrown by the {@link RestAPIMain#run()} method.  The value of {@link #getCause()} will always be the exact
     * cause of the underlying exception.
     */
    public class ServerRuntimeException extends RuntimeException {
        public ServerRuntimeException(final Throwable ex) {
            super(ex);
        }
    }

    /**
     * Thrown when bad arguments are passed to the {@link RestAPIMain#RestAPIMain(String[])} constructor or the
     * arguments supplied cannot be used to creat the server.
     */
    public class ProgramArgumentException extends IllegalArgumentException {

        public ProgramArgumentException() {}

        public ProgramArgumentException(final OptionException ex) {
            super(ex.getMessage(), ex);
        }

    }

    /**
     * Thrown when the options specified a request for help.
     */
    public class HelpRequestedException extends ProgramArgumentException {}

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
