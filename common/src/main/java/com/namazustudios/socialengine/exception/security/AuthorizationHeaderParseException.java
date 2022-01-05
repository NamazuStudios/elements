package com.namazustudios.socialengine.exception.security;

import com.namazustudios.socialengine.exception.ForbiddenException;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class AuthorizationHeaderParseException extends ForbiddenException {

    public AuthorizationHeaderParseException() {}

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
