package dev.getelements.elements.rt.remote;

public enum MessageType {

    /**
     * Indicates that the message contains an instance of {@link Invocation}
     */
    INVOCATION,

    /**
     * Indicates that the message contains an instance of {@link InvocationError}
     */
    INVOCATION_ERROR,

    /**
     * Indicates that the message contains an instance of {@link InvocationResult}
     */
    INVOCATION_RESULT

}
