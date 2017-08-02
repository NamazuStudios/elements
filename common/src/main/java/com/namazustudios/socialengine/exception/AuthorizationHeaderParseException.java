package com.namazustudios.socialengine.exception;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class AuthorizationHeaderParseException extends InvalidDataException {

    public AuthorizationHeaderParseException() {
    }

    public AuthorizationHeaderParseException(String s) {
        super(s);
    }

    public AuthorizationHeaderParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationHeaderParseException(Throwable cause) {
        super(cause);
    }

}
