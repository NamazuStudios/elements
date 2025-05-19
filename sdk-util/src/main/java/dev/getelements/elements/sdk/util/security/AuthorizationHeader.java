package dev.getelements.elements.sdk.util.security;

import dev.getelements.elements.sdk.model.exception.security.AuthorizationHeaderParseException;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
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
     * trigger an attempt to authorize the user via HTTP Basic auth
     */
    public static final String AUTH_TYPE_BEARER = "Bearer";

    private static final Pattern WHITESPACE = compile("\\s");

    private final String type;

    private final String credentials;

    public static <T> Optional<AuthorizationHeader> withValueSupplier(final Function<String, T> valueSupplier) {
        return withOptionalValueSupplier(headerName -> Optional.ofNullable(valueSupplier.apply(headerName)));
    }

    /**
     * Constructs an instance of {@link AuthorizationHeader} with the supplied {@link HeaderOptionalSupplier<T>}. If
     * the supplied {@link HeaderOptionalSupplier<T>} does not supply a header the returned
     * {@link Optional<AuthorizationHeader>} will be empty.
     *
     * @param headerOptionalSupplier the {@link HeaderOptionalSupplier<T>} supplying the header values
     * @param <T> the type of header result
     */
    public static <T> Optional<AuthorizationHeader> withOptionalValueSupplier(final HeaderOptionalSupplier<T> headerOptionalSupplier) {
        return headerOptionalSupplier.asString(AUTH_HEADER).map(AuthorizationHeader::new);
    }

    /**
     * Parses the supplied header string, throwing an instance of {@link AuthorizationHeaderParseException} if the
     * header does not correctly parse.
     *
     * @param header the header
     */
    public AuthorizationHeader(final String header) {

        final var headerComponents = Stream.of(WHITESPACE.split(header))
                .filter(not(String::isBlank))
                .map(String::trim)
                .iterator();

        if (!headerComponents.hasNext()) {
            throw new AuthorizationHeaderParseException("Unable to determine auth type.");
        }

        final var first= headerComponents.next();

        if (headerComponents.hasNext()) {

            // If we have components following the token type, then we are able to go determine the auth header type.

            type = first;
            credentials = headerComponents.next();

            if (headerComponents.hasNext()) {
                // The spec for the authorization header says that anything following the credentials is invalid
                // so any trailing non-whitespace characters we presume this is a bad header and throw the instance
                // of AuthorizationHeaderParseException
                throw new AuthorizationHeaderParseException("Unexpected no more header components after credentials.");
            }

        } else {
            // If there is nothing here, we just presume that the token is a bearer token. I think this is a somewhat
            // loose interpretation of the specification and many APIs accept the token if "Bearer" is omitted.
            credentials = first;
            type = AUTH_TYPE_BEARER;
        }

    }

    public String getType() {
        return type;
    }

    /**
     * Returns the credentials as a basic authorization header.
     *
     * @param encoding the encoding to use when parsing the header.
     *
     * @return the {@link BasicAuthorizationHeader}
     * @throws AuthorizationHeaderParseException if it is not possible to construct the instance of {@link BasicAuthorizationHeader}
     * @throws AuthorizationHeaderParseException if it is not possible to construct the instance of {@link BasicAuthorizationHeader}
     */
    public BasicAuthorizationHeader asBasicHeader(final String encoding) {

        if (!AUTH_TYPE_BASIC.equals(getType())) {
            throw new AuthorizationHeaderParseException(getType() + " not for Basic");
        }

        return new BasicAuthorizationHeader(encoding, credentials);

    }

    /**
     * Returns the credentials as a bearer token authorization header.
     *
     * @return the {@link BearerAuthorizationHeader}
     * @throws AuthorizationHeaderParseException if it is not possible to construct the instance of {@link BearerAuthorizationHeader}
     */
    public BearerAuthorizationHeader asBearerHeader() {

        if (!AUTH_TYPE_BEARER.equals(getType())) {
            throw new AuthorizationHeaderParseException(getType() + " not suitable for Bearer");
        }

        return new BearerAuthorizationHeader(credentials);

    }

}
