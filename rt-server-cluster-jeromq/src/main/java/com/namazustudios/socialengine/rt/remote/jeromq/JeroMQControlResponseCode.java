package com.namazustudios.socialengine.rt.remote.jeromq;

public enum JeroMQControlResponseCode {

    /**
     * Indicates that the response was okay.
     */
    OK,

    /**
     * Indicates that the client issued an unknown command.
     */
    UNKNOWN_COMMAND,

    /**
     * Indicates there was no such route.
     */
    NO_SUCH_ROUTE,

    /**
     * There was an exception processing the request.
     */
    EXCEPTION,

    /**
     * Indicates an unknown error.
     */
    UNKNOWN_ERROR

}
