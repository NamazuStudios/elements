package com.namazustudios.socialengine.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.security.AuthorizationHeaderParseException;
import com.namazustudios.socialengine.exception.security.SessionExpiredException;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class JWTCredentials {

    private final DecodedJWT decoded;

    /**
     * Generic JWT Credentials.
     *
     * @param token the token to parse.
     */
    public JWTCredentials(final String token) {

        try {
            decoded = JWT.decode(token);
        } catch (JWTDecodeException ex) {
            throw new AuthorizationHeaderParseException(ex);
        }

        final var now = new Date();
        final var exp = decoded.getExpiresAt();
        final var nbf = decoded.getNotBefore();

        if (exp != null && exp.before(now)) {
            throw new SessionExpiredException("JWT Token Expired.");
        }

        if (nbf != null && nbf.after(now)) {
            throw new ForbiddenException("Premature use of token.");
        }

    }

    /**
     * Gets the {@link DecodedJWT} backing this object.
     *
     * @return the decoded JWT
     */
    public DecodedJWT getDecoded() {
        return decoded;
    }

    /**
     * Finds the issuer of the token.
     *
     * @return an {@link Optional<String>}
     */
    public Optional<String> findIssuer() {
        return Optional.ofNullable(decoded.getIssuer());
    }

    /**
     * Finds the audience of the token.
     *
     * @return an {@link Optional<List<String>>}
     */
    public Optional<List<String>> findAudience() {
        return Optional.ofNullable(decoded.getAudience());
    }

    /**
     * Finds the expiration date of the token.
     *
     * @return an {@link Optional<Date>}
     */
    public Optional<Date> findExpirationDate() {
        return Optional.of(decoded.getExpiresAt());
    }

    /**
     * Verifies the JWT has the required data, and that the signature matches
     *
     * @return if the JWT token is valid
     */
    public boolean verify(final Algorithm algorithm) {
        try {
            algorithm.verify(decoded);
            return true;
        } catch (SignatureVerificationException ex) {
            return false;
        }
    }

    /**
     * Converts this {@link JWTCredentials} to {@link CustomJWTCredentials}, throwing the appropriate exceptions if the
     * conversion is not appropriate for this instance.
     *
     * @return {@link CustomJWTCredentials}, if the supplied credentials are valid
     */
    public CustomJWTCredentials asCustomCredentials() {
        return new CustomJWTCredentials(this);
    }

}
