package com.namazustudios.socialengine.cdnserve;

import com.namazustudios.socialengine.cdnserve.guice.ServerModule;
import com.namazustudios.socialengine.codeserve.CodeServeModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.rt.git.BareBootstrapResourcesModule;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;

import java.util.concurrent.Callable;

import static com.google.inject.Guice.createInjector;
import static org.slf4j.LoggerFactory.getLogger;

public class CdnServeMain implements Callable<Void>, Runnable {

    private static final Logger logger = getLogger(CdnServeMain.class);

    private Server server = new Server();

    /**
     * Args style constructor.
     *
     * @param args the command-line arguments
     * @throws ProgramArgumentException if there was a problem parsing the command line arguments
     */
    public CdnServeMain(final String[] args) throws ProgramArgumentException {

        final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        server = createInjector(
            new ServerModule(),
            new BareBootstrapResourcesModule(),
            new CodeServeModule(defaultConfigurationSupplier)
        ).getInstance(Server.class);

    }

    /**
     * Runs this RestAPI and blocks until the server exits.
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
     * Thrown by the {@link CdnServeMain#run()} method.  The value of {@link #getCause()} will always be the exact
     * cause of the underlying exception.
     */
    public class ServerRuntimeException extends RuntimeException {
        public ServerRuntimeException(final Throwable ex) {
            super(ex);
        }
    }

    /**
     * Thrown when bad arguments are passed to the {@link CdnServeMain#CdnServeMain(String[])} constructor or the
     * arguments supplied cannot be used to creat the server.
     */
    public class ProgramArgumentException extends IllegalArgumentException {

        public ProgramArgumentException() {}

    }

    /**
     * Thrown when the options specified a request for help.
     */
    public class HelpRequestedException extends ProgramArgumentException {}

    public static void main(final String[] args) throws Exception {
        try {
            final CdnServeMain main = new CdnServeMain(args);
            main.run();
        } catch (final ProgramArgumentException ex) {
            logger.debug("Bad program arguments.", ex);
        }
    }

}
