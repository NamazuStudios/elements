package dev.getelements.elements.service.auth.oidc.provider;

import com.nimbusds.jose.jwk.RSAKey;
import dev.getelements.elements.sdk.dao.JwtSigningKeyDao;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.JWKSet;
import dev.getelements.elements.sdk.model.auth.JwtSigningKey;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.service.auth.OidcProviderJwksService;
import dev.getelements.elements.service.auth.StandardSessionTokenKeySupport;
import jakarta.inject.Inject;

import java.security.interfaces.RSAPublicKey;

public class StandardOidcProviderJwksService implements OidcProviderJwksService {

    private JwtSigningKeyDao jwtSigningKeyDao;

    private StandardSessionTokenKeySupport sessionTokenKeySupport;

    @Override
    public JWKSet getJwks() {

        // Every key regardless of status: tokens issued under a since-retired key must continue to verify
        // for their remaining lifetime, so the JWKS must keep publishing its public half.

        final var keys = getJwtSigningKeyDao().getSigningKeys()
                .stream()
                .map(this::toJwk)
                .toList();

        final var jwkSet = new JWKSet();
        jwkSet.setKeys(keys);
        return jwkSet;

    }

    private JWK toJwk(final JwtSigningKey signingKey) {

        try {

            final var publicKey = (RSAPublicKey) getSessionTokenKeySupport().decodePublicKey(signingKey.getPublicKey());
            final var nimbusKey = new RSAKey.Builder(publicKey)
                    .keyID(signingKey.getKid())
                    .build();

            return new JWK(
                    signingKey.getAlgorithm(),
                    signingKey.getKid(),
                    nimbusKey.getKeyType().getValue(),
                    "sig",
                    nimbusKey.getPublicExponent().toString(),
                    nimbusKey.getModulus().toString());

        } catch (RuntimeException ex) {
            throw new InternalException("Unable to publish public signing key " + signingKey.getKid(), ex);
        }

    }

    public JwtSigningKeyDao getJwtSigningKeyDao() {
        return jwtSigningKeyDao;
    }

    @Inject
    public void setJwtSigningKeyDao(JwtSigningKeyDao jwtSigningKeyDao) {
        this.jwtSigningKeyDao = jwtSigningKeyDao;
    }

    public StandardSessionTokenKeySupport getSessionTokenKeySupport() {
        return sessionTokenKeySupport;
    }

    @Inject
    public void setSessionTokenKeySupport(StandardSessionTokenKeySupport sessionTokenKeySupport) {
        this.sessionTokenKeySupport = sessionTokenKeySupport;
    }

}
