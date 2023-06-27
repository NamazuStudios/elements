package dev.getelements.elements.setup;

/**
 * An interface for the sub-commands.  The first argument is stripped from
 * the command array and passed to this command.
 *
 * Created by patricktwohig on 4/8/15.
 */
public interface SetupCommand extends AutoCloseable {

    String STDIN = "dev.getelements.elements.setup.stdin";

    String STDOUT = "dev.getelements.elements.setup.stdout";

    String STDERR = "dev.getelements.elements.setup.stderr";

    /**
     * Passed to the command after it has been instantiated by the IoC system.
     *
     * @param args
     * @throws Exception
     */
    void run(final String[] args) throws Exception;

    /**
     * Closes the command.
     */
    default void close() throws Exception {}

}
