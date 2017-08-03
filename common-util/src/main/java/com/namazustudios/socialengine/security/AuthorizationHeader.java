package com.namazustudios.socialengine.security;

import com.google.common.base.Splitter;
import com.namazustudios.socialengine.exception.AuthorizationHeaderParseException;

import java.util.Iterator;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class AuthorizationHeader {

    /**
     * Constant for the Authorization header.
     */
    public static final String AUTH_HEADER = "Authorization";

    /**
     * Used in conjunction with the standard Authorization header.  This is used to
     * trigger an attempt to authorize the user via HTTP Basic auth
     */
    public static final String AUTH_TYPE_BASIC = "Basic";

    /**
     * Used in conjunction with the standard Authorization header.  This is used to
     * trigger an attempt to authorize the user via Facebook OAuth tokens.
     */
    public static final String AUTH_TYPE_FACEBOOK = "Facebook";

    private static final Pattern WHITESPACE = compile("\\s");

    private final String type;

    private final String credentials;

    public AuthorizationHeader(String header) {

        final Iterator<String> headerComponents = Splitter.on(WHITESPACE)
            .trimResults()
            .omitEmptyStrings()
            .split(header)
            .iterator();

        if (!headerComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Unable to determine auth type.");
        }

        type = headerComponents.next();

        if (!headerComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Unable to find credentials.");
        }

        credentials = headerComponents.next();

        if (headerComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Unexpected header components after credentials.");
        }

    }

    public String getType() {
        return type;
    }

    public FacebookAuthorizationHeader asFacebookAuthHeader() {

        if (!AUTH_TYPE_FACEBOOK.equals(getType())) {
            throw new AuthorizationHeaderParseException(getType() + " not suitable for Facebook");
        }

        return new FacebookAuthorizationHeader(credentials);

    }

    public BasicAuthorizationHeader asBasicHeader(final String encoding) {

        if (!AUTH_TYPE_BASIC.equals(getType())) {
            throw new AuthorizationHeaderParseException(getType() + " not suitable for Facebook");
        }

        return new BasicAuthorizationHeader(encoding, credentials);

    }

}
