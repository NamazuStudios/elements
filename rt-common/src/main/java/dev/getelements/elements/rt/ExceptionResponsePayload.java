package dev.getelements.elements.rt;

/**
 * Used to relay an {@link Exception} across the wire.  This includes only the.
 *
 * Created by patricktwohig on 9/30/15.
 */
public interface ExceptionResponsePayload {

    /**
     * Returns a message describing the problem.
     *
     * @return the message
     */
    String getMessage();

    /**
     * Gets the UUID of the exception response.  This is useful when trying to match
     * client and server logs.
     *
     * @return the UUID.
     */
    String getUUID();

}
