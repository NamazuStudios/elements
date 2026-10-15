package dev.getelements.elements.service.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dev.getelements.elements.sdk.dao.JwtSigningKeyDao;
import dev.getelements.elements.sdk.model.auth.JwtSigningKey;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.service.Constants;
import dev.getelements.elements.sdk.service.auth.SessionTokenIssuer;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.security.interfaces.RSAPrivateKey;
import com.nimbusds.jose.JOSEException;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

/**
 * The shared issuer for all session-creating login paths. Creates the server-side session record
 * via the {@link dev.getelements.elements.sdk.dao.SessionDao} and signs a session token carrying
 * the session's identity, replacing the record id handed back by the DAO with the signed token in
 * the returned {@link SessionCreation}.
 */
public class StandardSessionTokenIssuer implements SessionTokenIssuer {

    private StandardSessionTokenKeySupport sessionTokenKeySupport;

    private dev.getelements.elements.sdk.dao.SessionDao sessionDao;

    private String issuer;

    private String audience;

    @Override
    public SessionCreation issue(final Session session) {

        final var sessionCreation = getSessionDao().create(session);

        final var sessionToken = signSessionToken(sessionCreation.getSessionSecret(), session);

        final var issued = new SessionCreation();
        issued.setSessionSecret(sessionToken);
        issued.setSession(sessionCreation.getSession());

        return issued;

    }

    private String signSessionToken(final String sessionId, final Session session) {

        try {

            final var signingKey = getSessionTokenKeySupport().getActiveSigningKey();
            final var privateKey = (RSAPrivateKey) getSessionTokenKeySupport()
                    .getPrivateKey(signingKey);

            // JWT timestamps are second-precision; floor the session's millisecond expiry so that
            // the token's exp claim is an exact, deliberate value.

            final var expirySeconds = session.getExpiry() / 1000 * 1000;

            final var claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(session.getUser().getId())
                    .issuer(issuer)
                    .audience(audience)
                    .issueTime(new Date(currentTimeMillis()))
                    .expirationTime(new Date(expirySeconds))
                    .claim("sid", sessionId)
                    .claim("elm_lvl", session.getUser().getLevel().name());

            if (session.getProfile() != null) {
                claimsBuilder.claim("elm_pid", session.getProfile().getId());
            }

            if (session.getApplication() != null) {
                claimsBuilder.claim("elm_aid", session.getApplication().getId());
            }

            final var signedJwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKid()).build(),
                    claimsBuilder.build());

            signedJwt.sign(new RSASSASigner(privateKey));

            return signedJwt.serialize();

        } catch (JOSEException ex) {
            throw new InternalException(ex);
        }

    }

    public StandardSessionTokenKeySupport getSessionTokenKeySupport() {
        return sessionTokenKeySupport;
    }

    @Inject
    public void setSessionTokenKeySupport(StandardSessionTokenKeySupport sessionTokenKeySupport) {
        this.sessionTokenKeySupport = sessionTokenKeySupport;
    }

    public dev.getelements.elements.sdk.dao.SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(dev.getelements.elements.sdk.dao.SessionDao sessionDao) {
        this.sessionDao = sessionDao;
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
