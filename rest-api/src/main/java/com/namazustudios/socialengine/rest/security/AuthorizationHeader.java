package com.namazustudios.socialengine.rest.security;

import com.google.common.base.Splitter;

import java.util.Iterator;
import java.util.regex.Pattern;

import static com.namazustudios.socialengine.rest.XHttpHeaders.AUTH_TYPE_FACEBOOK;
import static java.util.regex.Pattern.compile;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class AuthorizationHeader {

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

}
