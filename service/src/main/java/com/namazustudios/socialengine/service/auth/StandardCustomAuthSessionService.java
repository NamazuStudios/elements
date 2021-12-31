package com.namazustudios.socialengine.service.auth;

import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.dao.AuthSchemeDao;
import com.namazustudios.socialengine.dao.CustomAuthUserDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.ValidationFailureException;
import com.namazustudios.socialengine.exception.security.AuthorizationHeaderParseException;
import com.namazustudios.socialengine.model.auth.AuthScheme;
import com.namazustudios.socialengine.model.auth.UserClaim;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.security.CustomJWTCredentials;
import com.namazustudios.socialengine.security.JWTCredentials;
import com.namazustudios.socialengine.service.CustomAuthSessionService;
import com.namazustudios.socialengine.service.NameService;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class StandardCustomAuthSessionService implements CustomAuthSessionService {

    private NameService nameService;

    private ObjectMapper objectMapper;

    private AuthSchemeDao authSchemeDao;

    private CryptoKeyUtility cryptoKeyUtility;

    private ValidationHelper validationHelper;

    private CustomAuthUserDao customAuthUserDao;

    @Override
    public Session getSession(final String jwt) {

        final var credentials = new JWTCredentials(jwt);
        final var audience = credentials.findAudience().orElse(List.of());
        final var schemes = getAuthSchemeDao().getAuthSchemesByAudience(audience);

        return schemes
            .stream()
            .filter(scheme -> isValidIssuer(credentials, scheme))
            .filter(scheme -> isValidSignature(credentials, scheme))
            .findFirst()
            .map(scheme -> jwtToSession(credentials.asCustomCredentials(), scheme))
            .orElseThrow(ForbiddenException::new);

    }

    private boolean isValidSignature(final JWTCredentials decoded, final AuthScheme scheme) {
        switch (scheme.getAlgorithm()) {
            case RSA_256:
                return isValidRSA(decoded, scheme, k -> Algorithm.RSA256(k, null));
            case RSA_384:
                return isValidRSA(decoded, scheme, k -> Algorithm.RSA384(k, null));
            case RSA_512:
                return isValidRSA(decoded, scheme, k -> Algorithm.RSA512(k, null));
            case ECDSA_256:
                return isValidECDSA(decoded, scheme, k -> Algorithm.ECDSA256(k, null));
            case ECDSA_384:
                return isValidECDSA(decoded, scheme, k -> Algorithm.ECDSA384(k, null));
            case ECDSA_512:
                return isValidECDSA(decoded, scheme, k -> Algorithm.ECDSA512(k, null));
            default:
                throw new InternalException("Unsupported algorithm: " + scheme.getAlgorithm());
        }
    }

    private boolean isValidIssuer(final JWTCredentials credentials, final AuthScheme scheme) {
        final var issuer = credentials
            .findIssuer()
            .orElseThrow(() -> new AuthorizationHeaderParseException("Must specify issuer."));
        return scheme.getAllowedIssuers().contains(issuer);
    }

    private boolean isValidRSA(final JWTCredentials decoded,
                               final AuthScheme scheme,
                               final Function<RSAPublicKey, Algorithm> algorithmFactory) {

        final var key = getCryptoKeyUtility().getPublicKey(
            scheme.getAlgorithm(),
            scheme.getPublicKey(),
            RSAPublicKey.class
        );

        final var algo = algorithmFactory.apply(key);
        return decoded.verify(algo);

    }

    private boolean isValidECDSA(final JWTCredentials credentials,
                                 final AuthScheme scheme,
                                 final Function<ECPublicKey, Algorithm> algorithmFactory) {

        final var key = getCryptoKeyUtility().getPublicKey(
            scheme.getAlgorithm(),
            scheme.getPublicKey(),
            ECPublicKey.class
        );

        final var algo = algorithmFactory.apply(key);
        return credentials.verify(algo);

    }

    private Session jwtToSession(final CustomJWTCredentials credentials, final AuthScheme scheme) {

        final var userKey = credentials.getUserKey();
        final var subject = credentials.getSubject();
        final var userClaim = getObjectMapper().convertValue(credentials.getUser(), UserClaim.class);

        try {
            getValidationHelper().validateModel(userClaim);
        } catch (ValidationFailureException ex) {
            throw new AuthorizationHeaderParseException(ex);
        }

        if (userClaim.getName() == null || userClaim.getEmail() == null) {
            final var name = getNameService().generateQualifiedName();
            if (userClaim.getName() == null) userClaim.setName(name);
            if (userClaim.getEmail() == null) userClaim.setEmail(getNameService().formatAnonymousEmail(name));
        }

        if (scheme.getUserLevel().compareTo(userClaim.getLevel()) < 0)
            throw new ForbiddenException("Cannot assign user level: " + userClaim.getLevel());

        final var user = getCustomAuthUserDao().upsertUser(userKey, subject, userClaim);

        final var session = new Session();
        final var exp = credentials
            .getCredentials()
            .findExpirationDate()
            .orElseThrow(() -> new AuthorizationHeaderParseException("Missing exp claim."));

        session.setUser(user);
        session.setExpiry(exp.getTime());

        return session;
    }

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuthSchemeDao getAuthSchemeDao() {
        return authSchemeDao;
    }

    @Inject
    public void setAuthSchemeDao(AuthSchemeDao authSchemeDao) {
        this.authSchemeDao = authSchemeDao;
    }

    public CryptoKeyUtility getCryptoKeyUtility() {
        return cryptoKeyUtility;
    }

    @Inject
    public void setCryptoKeyUtility(CryptoKeyUtility cryptoKeyUtility) {
        this.cryptoKeyUtility = cryptoKeyUtility;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public CustomAuthUserDao getCustomAuthUserDao() {
        return customAuthUserDao;
    }

    @Inject
    public void setCustomAuthUserDao(CustomAuthUserDao customAuthUserDao) {
        this.customAuthUserDao = customAuthUserDao;
    }

}
