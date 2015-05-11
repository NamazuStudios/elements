package com.namazustudios.socialengine;

/**
 * An interface for the sub-commands.  The first argument is stripped from
 * the command array and passed to this command.
 *
 * Created by patricktwohig on 4/8/15.
 */
public interface Command  {

    /**
     * Passed to the command after it has been instantiated by the IoC system.
     *
     * @param args
     * @throws Exception
     */
    void run(final String[] args) throws Exception;

}
