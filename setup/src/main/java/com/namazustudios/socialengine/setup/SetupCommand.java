package com.namazustudios.socialengine.setup;

/**
 * An interface for the sub-commands.  The first argument is stripped from
 * the command array and passed to this command.
 *
 * Created by patricktwohig on 4/8/15.
 */
public interface SetupCommand extends AutoCloseable {

    String STDIN = "com.namazustudios.socialengine.setup.stdin";

    String STDOUT = "com.namazustudios.socialengine.setup.stdout";

    String STDERR = "com.namazustudios.socialengine.setup.stderr";

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
