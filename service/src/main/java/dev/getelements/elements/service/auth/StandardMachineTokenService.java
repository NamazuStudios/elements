package dev.getelements.elements.service.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dev.getelements.elements.sdk.dao.MachineTokenRevocationDao;
import dev.getelements.elements.sdk.model.auth.MachineToken;
import dev.getelements.elements.sdk.model.auth.MachineTokenClaims;
import dev.getelements.elements.sdk.model.auth.MachineTokenRequest;
import dev.getelements.elements.sdk.model.exception.ConflictException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import dev.getelements.elements.sdk.service.Constants;
import dev.getelements.elements.sdk.service.auth.MachineTokenService;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;

/**
 * Signs and verifies machine tokens with the deployment's active OIDC signing key. The token is an RS256
 * JWT carrying the standard JOSE claims plus the caller's domain-specific claims; revocation is a
 * TTL-indexed denylist keyed by {@code jti}.
 */
public class StandardMachineTokenService implements MachineTokenService {

    private static final Set<String> RESERVED_CLAIMS = Set.of(
            "iss", "sub", "aud", "exp", "nbf", "iat", "jti", "azp", "nonce", "auth_time",
            "sid", "elm_lvl", "elm_pid", "elm_aid");

    private MachineTokenRevocationDao machineTokenRevocationDao;

    private StandardSessionTokenKeySupport sessionTokenKeySupport;

    private String issuer;

    @Override
    public MachineToken issue(final MachineTokenRequest request) {

        validate(request);

        final var now = currentTimeMillis();
        final var expiry = now + request.getTtlSeconds() * 1000L;
        final var tokenId = resolveTokenId(request.getTokenId());

        final var claimsBuilder = new JWTClaimsSet.Builder()
                .subject(request.getSubject())
                .issuer(getIssuer())
                .audience(request.getAudience())
                .issueTime(new Date(now))
                .expirationTime(new Date(expiry))
                .jwtID(tokenId);

        if (request.getClaims() != null) {
            request.getClaims().forEach(claimsBuilder::claim);
        }

        final var token = new MachineToken();
        token.setToken(signToken(claimsBuilder.build()));
        token.setTokenId(tokenId);
        token.setSubject(request.getSubject());
        token.setIssuer(getIssuer());
        token.setAudience(request.getAudience());
        token.setIssuedAt(now);
        token.setExpiry(expiry);
        token.setClaims(request.getClaims() == null
                ? Map.of()
                : new LinkedHashMap<>(request.getClaims()));

        return token;

    }

    @Override
    public MachineTokenClaims verify(final String token, final String expectedAudience) {

        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Machine token is required.");
        }

        if (expectedAudience == null || expectedAudience.isBlank()) {
            throw new InvalidParameterException("Expected audience is required.");
        }

        final SignedJWT signedJwt;

        try {
            signedJwt = SignedJWT.parse(token);
        } catch (ParseException ex) {
            throw new UnauthorizedException("Malformed machine token.", ex);
        }

        final var kid = signedJwt.getHeader().getKeyID();

        if (kid == null) {
            throw new UnauthorizedException("Missing signing key id.");
        }

        final RSAPublicKey publicKey = getSessionTokenKeySupport().getPublicKey(kid);

        try {
            if (!signedJwt.verify(new RSASSAVerifier(publicKey))) {
                throw new UnauthorizedException("Invalid machine token signature.");
            }
        } catch (JOSEException ex) {
            throw new UnauthorizedException("Invalid machine token signature.", ex);
        }

        final JWTClaimsSet claimsSet;

        try {
            claimsSet = signedJwt.getJWTClaimsSet();
        } catch (ParseException ex) {
            throw new UnauthorizedException("Malformed machine token claims.", ex);
        }

        final var expirationTime = claimsSet.getExpirationTime();

        if (expirationTime == null) {
            throw new UnauthorizedException("Missing exp claim.");
        } else if (expirationTime.before(new Date(currentTimeMillis()))) {
            throw new UnauthorizedException("Machine token expired.");
        }

        if (!getIssuer().equals(claimsSet.getIssuer())) {
            throw new UnauthorizedException("Invalid machine token issuer.");
        }

        if (claimsSet.getAudience() == null || !claimsSet.getAudience().contains(expectedAudience)) {
            throw new UnauthorizedException("Invalid machine token audience.");
        }

        final var tokenId = claimsSet.getJWTID();

        if (tokenId == null || tokenId.isBlank()) {
            throw new UnauthorizedException("Missing jti claim.");
        }

        if (getMachineTokenRevocationDao().isRevoked(tokenId)) {
            throw new UnauthorizedException("Machine token has been revoked.");
        }

        final var claims = new MachineTokenClaims();
        claims.setTokenId(tokenId);
        claims.setSubject(claimsSet.getSubject());
        claims.setIssuer(claimsSet.getIssuer());
        claims.setAudience(expectedAudience);
        claims.setIssuedAt(claimsSet.getIssueTime() == null ? 0 : claimsSet.getIssueTime().getTime());
        claims.setExpiry(expirationTime.getTime());
        claims.setClaims(extractCustomClaims(claimsSet));

        return claims;

    }

    @Override
    public void revoke(final String tokenId, final long expiresAtMillis) {

        if (tokenId == null || tokenId.isBlank()) {
            throw new InvalidParameterException("Token id is required.");
        }

        // A token that has already expired cannot verify, so there is nothing to denylist. Its TTL entry
        // would be swept immediately anyway.

        if (expiresAtMillis <= currentTimeMillis()) {
            return;
        }

        getMachineTokenRevocationDao().revoke(tokenId, new Timestamp(expiresAtMillis));

    }

    private String resolveTokenId(final String supplied) {

        if (supplied == null || supplied.isBlank()) {
            return UUID.randomUUID().toString();
        }

        if (getMachineTokenRevocationDao().isRevoked(supplied)) {
            throw new ConflictException("Token id is already revoked: " + supplied);
        }

        return supplied;

    }

    private void validate(final MachineTokenRequest request) {

        if (request == null) {
            throw new InvalidParameterException("Machine token request is required.");
        }

        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new InvalidParameterException("subject is required.");
        }

        if (request.getAudience() == null || request.getAudience().isBlank()) {
            throw new InvalidParameterException("audience is required.");
        }

        if (request.getTtlSeconds() <= 0) {
            throw new InvalidParameterException("ttlSeconds must be positive.");
        }

        if (request.getClaims() == null) {
            return;
        }

        request.getClaims().forEach((name, value) -> {

            if (name == null || name.isBlank()) {
                throw new InvalidParameterException("Claim name must not be blank.");
            }

            if (RESERVED_CLAIMS.contains(name)) {
                throw new InvalidParameterException("Claim name is reserved: " + name);
            }

            if (!isSupportedClaimValue(value)) {
                throw new InvalidParameterException("Unsupported value for claim: " + name);
            }

        });

    }

    private Map<String, Object> extractCustomClaims(final JWTClaimsSet claimsSet) {

        final var claims = new LinkedHashMap<String, Object>();

        claimsSet.getClaims().forEach((name, value) -> {
            if (!RESERVED_CLAIMS.contains(name)) {
                claims.put(name, normalize(value));
            }
        });

        return claims;

    }

    /**
     * Converts Nimbus's JSON container types into plain Java collections so callers do not have to depend on
     * the JSON library.
     */
    private Object normalize(final Object value) {

        if (value instanceof Map<?, ?> map) {
            final var normalized = new LinkedHashMap<String, Object>();
            map.forEach((key, nested) -> normalized.put(String.valueOf(key), normalize(nested)));
            return normalized;
        }

        if (value instanceof Collection<?> collection) {
            final var normalized = new ArrayList<>(collection.size());
            collection.forEach(nested -> normalized.add(normalize(nested)));
            return normalized;
        }

        return value;

    }

    private static boolean isSupportedClaimValue(final Object value) {

        if (value instanceof String || value instanceof Boolean || value instanceof Number) {
            return true;
        }

        if (value instanceof Collection<?> collection) {
            return collection.stream().allMatch(StandardMachineTokenService::isSupportedClaimValue);
        }

        if (value instanceof Map<?, ?> map) {
            return map.keySet().stream().allMatch(String.class::isInstance)
                    && map.values().stream().allMatch(StandardMachineTokenService::isSupportedClaimValue);
        }

        return false;

    }

    private String signToken(final JWTClaimsSet claims) {

        try {

            final var signingKey = getSessionTokenKeySupport().getActiveSigningKey();
            final var privateKey = (RSAPrivateKey) getSessionTokenKeySupport().getPrivateKey(signingKey);

            final var signedJwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKid()).build(),
                    claims);

            signedJwt.sign(new RSASSASigner(privateKey));

            return signedJwt.serialize();

        } catch (JOSEException ex) {
            throw new InternalException(ex);
        }

    }

    public MachineTokenRevocationDao getMachineTokenRevocationDao() {
        return machineTokenRevocationDao;
    }

    @Inject
    public void setMachineTokenRevocationDao(MachineTokenRevocationDao machineTokenRevocationDao) {
        this.machineTokenRevocationDao = machineTokenRevocationDao;
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
    public void setIssuer(@Named(Constants.OIDC_PROVIDER_ISSUER) String issuer) {
        this.issuer = issuer;
    }

}
