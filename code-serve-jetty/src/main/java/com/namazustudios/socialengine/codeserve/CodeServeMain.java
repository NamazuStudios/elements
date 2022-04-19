package com.namazustudios.socialengine.codeserve;

import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
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
import org.slf4j.Logger;

import javax.servlet.DispatcherType;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.Stage.DEVELOPMENT;
import static java.util.EnumSet.allOf;
import static org.slf4j.LoggerFactory.getLogger;

public class CodeServeMain implements Callable<Void>, Runnable {

    private static final Logger logger = getLogger(CodeServeMain.class);

    private static final OptionParser optionParser = new OptionParser();

    public static final Stage DEFAULT_STAGE = DEVELOPMENT;

    private static final OptionSpec<Stage> stageOptionSpec = optionParser
            .accepts("stage", "Is this running in development or production?")
            .withOptionalArg()
            .ofType(Stage.class)
            .defaultsTo(DEFAULT_STAGE);

    private static final OptionSpec<Void> helpOptionSpec = optionParser
            .accepts("help", "Displays the help message.")
            .forHelp();

    private final Server server;

    /**
     * Args style constructor.
     *
     * @param args the command-line arguments
     * @throws ProgramArgumentException if there was a problem parsing the command line arguments
     */
    public CodeServeMain(final String[] args) throws ProgramArgumentException {

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
            new CodeServeGitServletModule(),
            new GitSecurityModule(),
            new CodeServeServerModule(),
            new LuaBootstrapResourcesModule(),
            new CodeServeModule((defaultConfigurationSupplier))
        );

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var servletHandler = injector.getInstance(ServletContextHandler.class);
        this.server = injector.getInstance(Server.class);
        doInit(guiceFilter, servletHandler);

    }

    private void doInit(final GuiceFilter guiceFilter,
                        final ServletContextHandler servletHandler) {

        servletHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));

        final var defaultInitParameters = new HashMap<String, String>();
        defaultInitParameters.put("dirAllowed", "false");

        final var defaultServletHolder = servletHandler.addServlet(DefaultServlet.class, "/");
        defaultServletHolder.setInitParameters(defaultInitParameters);

        final var handlerCollection = new HandlerCollection();
        handlerCollection.addHandler(servletHandler);

        if (!"/".equals(servletHandler.getContextPath())) {
            final var rootServletHandler = new ServletContextHandler();
            rootServletHandler.setContextPath("/");
            rootServletHandler.addServlet(HappyServlet.class, "/");
            handlerCollection.addHandler(rootServletHandler);
        }

        server.setHandler(handlerCollection);

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
