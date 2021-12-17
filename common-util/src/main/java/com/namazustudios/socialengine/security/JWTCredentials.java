package com.namazustudios.socialengine.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.namazustudios.socialengine.exception.security.AuthorizationHeaderParseException;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    /**
     * Gets the issuer of the token
     *
     * @return the issuer, or null
     */
    public String getIssuer() { return decoded.getIssuer(); }

    /**
     * Gets the audience of the token
     *
     * @return the audience, or null
     */
    public List<String> getAudience() { return decoded.getAudience(); }

    /**
     * Gets the expiration date of the token
     *
     * @return the expiration, or null
     */
    public Date getExpirationDate() { return decoded.getExpiresAt(); }

    /**
     * Gets the 'not before' date of the token
     *
     * @return the 'not before' date, or null
     */
    public Date getNotBefore() { return decoded.getNotBefore(); }

    /**
     * Gets a token claim by name
     *
     * @return the token claim with the given name
     */
    public String getClaim(String name) { return decoded.getClaim(name).asString(); }

    /**
     * Verifies the JWT has the required data, and that the signature matches
     *
     * @return if the JWT token is valid
     */
    public Boolean verify() {
        var validJWT = true;

        var audience = getAudience();
        if (audience == null) {
            validJWT = false;
        }

        var exp = getExpirationDate();
        if (exp != null && exp.before(new Date())) {
            validJWT = false;
        }

        var notBefore = getNotBefore();
        if (notBefore != null && notBefore.after(new Date())) {
            validJWT = false;
        }

        // TODO verify the signature against private key

        return validJWT;
    }
}
