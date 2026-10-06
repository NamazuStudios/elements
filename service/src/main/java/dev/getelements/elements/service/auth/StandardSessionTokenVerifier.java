package dev.getelements.elements.service.auth;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dev.getelements.elements.sdk.service.auth.SessionTokenVerifier;
import dev.getelements.elements.sdk.model.auth.SessionTokenClaims;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import dev.getelements.elements.sdk.model.exception.security.SessionExpiredException;
import dev.getelements.elements.sdk.service.Constants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

/**
 * Verifies Elements-issued session tokens locally: signature against the server's own signing keys
 * (selected by the token's {@code kid} header), then expiry, issuer, and audience. No database
 * access is required to verify a token.
 */
public class StandardSessionTokenVerifier implements SessionTokenVerifier {

    private static final String SID_CLAIM = "sid";

    private static final String LEVEL_CLAIM = "elm_lvl";

    private StandardSessionTokenKeySupport sessionTokenKeySupport;

    private String issuer;

    private String audience;

    @Override
    public boolean isSessionToken(final String credentials) {

        if (credentials == null) return false;

        // Matches the three-segment compact JOSE serialization while excluding legacy opaque
        // session secrets, which are plain hex strings and contain no separators.

        final var first = credentials.indexOf('.');
        if (first < 0) return false;

        final var second = credentials.indexOf('.', first + 1);
        if (second < 0) return false;

        return credentials.indexOf('.', second + 1) < 0
                && first > 0
                && second > first + 1
                && credentials.length() > second + 1;

    }

    @Override
    public SessionTokenClaims verify(final String sessionToken) {

        final SignedJWT signedJwt;

        try {
            signedJwt = SignedJWT.parse(sessionToken);
        } catch (ParseException ex) {
            throw new UnauthorizedException("Malformed session token.");
        }

        final var kid = signedJwt.getHeader().getKeyID();

        if (kid == null) {
            throw new UnauthorizedException("Missing signing key id.");
        }

        final RSAPublicKey publicKey = getSessionTokenKeySupport().getPublicKey(kid);

        try {
            if (!signedJwt.verify(new RSASSAVerifier(publicKey))) {
                throw new UnauthorizedException("Invalid session token signature.");
            }
        } catch (com.nimbusds.jose.JOSEException ex) {
            throw new UnauthorizedException("Invalid session token signature.");
        }

        final JWTClaimsSet claimsSet;

        try {
            claimsSet = signedJwt.getJWTClaimsSet();
        } catch (ParseException ex) {
            throw new UnauthorizedException("Malformed session token claims.");
        }

        final var expirationTime = claimsSet.getExpirationTime();

        if (expirationTime == null) {
            throw new UnauthorizedException("Missing exp claim.");
        } else if (expirationTime.before(new Date(currentTimeMillis()))) {
            throw new SessionExpiredException("Session token expired.");
        }

        if (!issuer.equals(claimsSet.getIssuer())) {
            throw new UnauthorizedException("Invalid token issuer.");
        }

        if (claimsSet.getAudience() == null || !claimsSet.getAudience().contains(audience)) {
            throw new UnauthorizedException("Invalid token audience.");
        }

        final var claims = new SessionTokenClaims();
        claims.setSubject(claimsSet.getSubject());
        claims.setIssuer(claimsSet.getIssuer());
        claims.setAudience(audience);
        claims.setIssuedAt(claimsSet.getIssueTime() == null ? 0 : claimsSet.getIssueTime().getTime());
        claims.setExpiry(expirationTime.getTime());

        try {
            claims.setSessionId(claimsSet.getStringClaim(SID_CLAIM));
            claims.setLevel(claimsSet.getStringClaim(LEVEL_CLAIM));
        } catch (ParseException ex) {
            throw new UnauthorizedException("Malformed session token claims.");
        }

        if (claims.getSessionId() == null || claims.getSessionId().isBlank()) {
            throw new UnauthorizedException("Missing sid claim.");
        }

        return claims;

    }

    public StandardSessionTokenKeySupport getSessionTokenKeySupport() {
        return sessionTokenKeySupport;
    }

    @Inject
    public void setSessionTokenKeySupport(StandardSessionTokenKeySupport sessionTokenKeySupport) {
        this.sessionTokenKeySupport = sessionTokenKeySupport;
    }

    public String getIssuer() {
        return issuer;
    }

    @Inject
    public void setIssuer(@Named(Constants.SESSION_TOKEN_ISSUER) String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    @Inject
    public void setAudience(@Named(Constants.SESSION_TOKEN_AUDIENCE) String audience) {
        this.audience = audience;
    }

}
