package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.InvalidDataException;
import org.omg.CORBA.DynAnyPackage.Invalid;

/**
 * A Request is a request sent to a particular resource.
 *
 * This ties the {@link RequestHeader} to a specific payload type for
 * the application.
 *
 * Created by patricktwohig on 7/26/15.
 */
public interface Request {

    /**
     * Gets ths {@link RequestHeader} object for this particular request.
     *
     * @return the header
     */
    RequestHeader getHeader();

    /**
     * Gets the payload object for this Request.
     *
     * @return the paylaod
     */
    Object getPayload();

    /**
     * Gets the payoad, cast to the given type.
     *
     * @param cls the type
     * @param <T>
     */
    <T> T getPayload(Class<T> cls);

    /**
     * A type whih validates any instance of {@link Request}.
     */
    class Validator {

        private Validator() {}

        /**
         * Checks that the request is valid.  A request is considered valid if the header as well
         * as it components are not null.
         *
         * @param request the request
         * @throws InvalidDataException if the request is not valid
         */
        public static void validate(final Request request) {

            final RequestHeader requestHeader = request.getHeader();

            if(requestHeader == null) {
                throw new InvalidDataException("request header null");
            } else if (requestHeader.getSequence() < 0) {
                throw new InvalidDataException("unexpected request sequence " + requestHeader.getSequence());
            } else if (requestHeader.getPath() == null) {
                throw new InvalidDataException("invalid path " + requestHeader.getPath());
            } else if (requestHeader.getMethod() == null) {
                throw new InvalidDataException("invalid method " + requestHeader.getMethod());
            } else if (requestHeader.getHeaders() == null) {
                throw new InvalidDataException("invalid request headers " + requestHeader.getHeaders());
            }

        }

    }

}
