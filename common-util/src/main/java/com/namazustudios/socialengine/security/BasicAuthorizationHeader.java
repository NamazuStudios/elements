package com.namazustudios.socialengine.security;

import com.google.common.base.Splitter;
import com.namazustudios.socialengine.exception.AuthorizationHeaderParseException;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Created by patricktwohig on 8/1/17.
 */
public class BasicAuthorizationHeader {

    private static final Pattern SEPARATOR = compile(":");

    private final String username;

    private final String password;

    public BasicAuthorizationHeader(final String encoding, final String header) {

        final String credentials;

        try {
            final byte[] decoded = Base64.decode(header);
            credentials = new String(decoded, encoding);
        } catch (Base64DecodingException e) {
            throw new AuthorizationHeaderParseException(e);
        } catch (UnsupportedEncodingException e) {
            throw new AuthorizationHeaderParseException(e);
        }

        final Iterator<String> credentialComponents = Splitter.on(SEPARATOR)
                .trimResults()
                .omitEmptyStrings()
                .split(credentials)
                .iterator();

        if (!credentialComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Credentials empty.");
        }

        username = credentialComponents.next();

        if (!credentialComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Password Not Specified");
        }

        password = credentialComponents.next();

        if (credentialComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Unexpected header components after password");
        }

    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the pass word.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

}
