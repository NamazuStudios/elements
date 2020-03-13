package com.namazustudios.socialengine.rest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.rest.guice.RestAPIModule;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;

import javax.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.inject.Stage.DEVELOPMENT;
import static com.namazustudios.socialengine.rest.guice.GuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static java.util.Collections.emptyList;
import static java.util.EnumSet.allOf;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;
import static org.eclipse.jetty.util.Loader.getResource;
import static org.slf4j.LoggerFactory.getLogger;

public class RestAPIMain implements Callable<Void>, Runnable {

    private static final Logger logger = getLogger(RestAPIMain.class);

    private static final OptionParser optionParser = new OptionParser();

    public static String DEFAULT_BIND_ADDRESS = "0.0.0.0";

    public static int DEFAULT_PORT = 8080;

    public static Stage DEFAULT_STAGE = DEVELOPMENT;

    public static String DEFAULT_API_CONTEXT = "/api";

    private static final OptionSpec<String> bindOptionSpec = optionParser
            .accepts("bind", "The bind address.")
            .withOptionalArg()
            .ofType(String.class)
            .defaultsTo(DEFAULT_BIND_ADDRESS);

    private static final OptionSpec<Integer> portOptionSpec = optionParser
            .accepts("port", "The TCP Port upon which to bind.")
            .withOptionalArg()
            .ofType(Integer.class)
            .defaultsTo(DEFAULT_PORT);

    private static final OptionSpec<String> apiContextOptionsSpec = optionParser
            .accepts("api-context", "The context upon which to run the api.")
            .withOptionalArg()
            .ofType(String.class)
            .defaultsTo(DEFAULT_API_CONTEXT);

    private static final OptionSpec<Stage> stageOptionSpec = optionParser
            .accepts("stage", "Is this running in development or production?")
            .withOptionalArg()
            .ofType(Stage.class)
            .defaultsTo(DEFAULT_STAGE);

    private static final OptionSpec<Void> helpOptionSpec = optionParser
            .accepts("help", "Displays the help message.")
            .forHelp();

    private final Server server = new Server();

    public RestAPIMain(final String[] args) throws ProgramArgumentException {

        int port;
        String bind;
        String apiContext;
        Stage stage;

        try {

            final OptionSet options = optionParser.parse(args);

            if (options.hasArgument(helpOptionSpec)) {
                throw new HelpRequestedException();
            }

            bind = options.valueOf(bindOptionSpec);
            port = options.valueOf(portOptionSpec);
            apiContext = options.valueOf(apiContextOptionsSpec);
            apiContext = apiContext.startsWith("/") ? apiContext : "/" + apiContext;

            stage = options.valueOf(stageOptionSpec);

        } catch (OptionException ex) {
            throw new ProgramArgumentException(ex);
        }

        init(port, bind, apiContext, stage, emptyList());

    }

    public RestAPIMain(final int port, final String bind,
                       final String apiContext,
                       final Stage stage, final List<Module> additionalModules) {
        init(port, bind, apiContext, stage, additionalModules);
    }

    private void init(final int port, final String bind,
                      final String apiContext,
                      final Stage stage, final List<Module> additionalModules) {

        final ServerConnector connector = new ServerConnector(server);
        connector.setHost(bind);
        connector.setPort(port);

        final HandlerCollection handlerCollection = new HandlerCollection();

        final ServletContextHandler servletHandler = new ServletContextHandler(SESSIONS);
        servletHandler.setContextPath(apiContext);

        final List<Module> moduleAggregate = new ArrayList<>();
        moduleAggregate.add(new RestAPIModule());
        moduleAggregate.addAll(additionalModules);

        final Injector injector = Guice.createInjector(stage, moduleAggregate);
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
