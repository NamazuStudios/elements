package com.namazustudios.socialengine.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.namazustudios.socialengine.exception.security.AuthorizationHeaderParseException;

public class JWTCredentials {

    private final DecodedJWT decoded;

    public JWTCredentials(final String token) {
        try {
            decoded = JWT.decode(token);
        } catch (JWTDecodeException ex) {
            throw new AuthorizationHeaderParseException(ex);
        }
    }

    /**
     * Gets the original token string.
     *
     * @return the token string
     */
    public String getToken() {
        return decoded.getToken();
    }

    /**
     * Gets the header bit of hte token.
     *
     * @return the header
     */
    public String getHeader() {
        return decoded.getHeader();
    }

    /**
     * Gets the payload bit of the token.
     *
     * @return the payload
     */
    public String getPayload() {
        return decoded.getPayload();
    }

    /**
     * Gets the signature of the token.
     *
     * @return the signature
     */
    public String getSignature() {
        return decoded.getSignature();
    }

}
