package dev.getelements.elements.sdk.util.security;

import dev.getelements.elements.sdk.model.exception.security.AuthorizationHeaderParseException;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Base64.getDecoder;
import static java.util.regex.Pattern.compile;

/**
 * Created by patricktwohig on 8/1/17.
 */
public class BasicAuthorizationHeader {

    /**
     * Constant for the default character encoding of the credentials (UTF-8)
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    private static final Pattern SEPARATOR = compile(":");

    private final String username;

    private final String password;

    public BasicAuthorizationHeader(final String encoding, final String header) {

        final String credentials;

        try {
            final byte[] decoded = getDecoder().decode(header);
            credentials = new String(decoded, encoding == null ? DEFAULT_ENCODING : encoding);
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            throw new AuthorizationHeaderParseException(e);
        }

        final Iterator<String> credentialComponents = Arrays.stream(SEPARATOR.split(credentials))
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
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
