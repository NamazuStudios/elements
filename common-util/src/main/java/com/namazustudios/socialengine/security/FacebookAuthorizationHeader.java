package com.namazustudios.socialengine.security;

import com.google.common.base.Splitter;
import com.namazustudios.socialengine.exception.AuthorizationHeaderParseException;

import java.util.Iterator;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class FacebookAuthorizationHeader {

    private static final Pattern SEPARATOR = compile(":");

    private final String accessToken;

    private final String applicationId;

    public FacebookAuthorizationHeader(final String credentials) {

        final Iterator<String> credentialComponents = Splitter.on(SEPARATOR)
                .trimResults()
                .omitEmptyStrings()
                .split(credentials)
                .iterator();

        if (!credentialComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Credentials empty.");
        }

        applicationId = credentialComponents.next();

        if (!credentialComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Unable to find access token.");
        }

        accessToken = credentialComponents.next();

        if (credentialComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Unexpected header components after accessToken.");
        }

    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getApplicationId() {
        return applicationId;
    }

}
