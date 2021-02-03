package com.namazustudios.socialengine.codeserve;

import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.rt.guice.RTGitBootstrapModule;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.Stage.DEVELOPMENT;
import static java.util.EnumSet.allOf;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;
import static org.eclipse.jetty.util.Loader.getResource;
import static org.slf4j.LoggerFactory.getLogger;

public class CodeServeMain implements Callable<Void>, Runnable {

    private static final Logger logger = getLogger(CodeServeMain.class);

    private static final OptionParser optionParser = new OptionParser();

    public static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";

    public static final int DEFAULT_PORT = 8082;

    public static final Stage DEFAULT_STAGE = DEVELOPMENT;

    public static final String DEFAULT_API_CONTEXT = "/code-serve";

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

    private final Injector injector;

    private final Server server = new Server();

    /**
     * Args style constructor.
     *
     * @param args the command-line arguments
     * @throws ProgramArgumentException if there was a problem parsing the command line arguments
     */
    public CodeServeMain(final String[] args) throws ProgramArgumentException {

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

        final DefaultConfigurationSupplier defaultConfigurationSupplier = new DefaultConfigurationSupplier();
        injector = createInjector(stage, new CodeServeModule(defaultConfigurationSupplier).withModule(new RTGitBootstrapModule()).withModule(new GitServletModule()).withModule(new GitSecurityModule()));

        final ServerConnector connector = new ServerConnector(server);
        connector.setHost(bind);
        connector.setPort(port);

        final HandlerCollection handlerCollection = new HandlerCollection();

        final ServletContextHandler servletHandler = new ServletContextHandler(SESSIONS);
        servletHandler.setContextPath(apiContext);

        final GuiceFilter guiceFilter = injector.getInstance(GuiceFilter.class);
        servletHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));

        final Map<String, String> defaultInitParameters = new HashMap<>();
        defaultInitParameters.put("dirAllowed", "false");

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
     * Thrown by the {@link CodeServeMain#run()} method.  The value of {@link #getCause()} will always be the exact
     * cause of the underlying exception.
     */
    public class ServerRuntimeException extends RuntimeException {
        public ServerRuntimeException(final Throwable ex) {
            super(ex);
        }
    }

    /**
     * Thrown when bad arguments are passed to the {@link CodeServeMain#CodeServeMain(String[])} constructor or the
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
            final CodeServeMain main = new CodeServeMain(args);
            main.run();
        } catch (final ProgramArgumentException ex) {
            logger.debug("Bad program arguments.", ex);
            optionParser.printHelpOn(System.out);
        }
    }

}
