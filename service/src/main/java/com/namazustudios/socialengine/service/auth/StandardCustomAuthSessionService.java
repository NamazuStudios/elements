package com.namazustudios.socialengine.service.auth;

import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.dao.AuthSchemeDao;
import com.namazustudios.socialengine.dao.CustomAuthUserDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.auth.AuthScheme;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.security.JWTCredentials;
import com.namazustudios.socialengine.service.CustomAuthSessionService;

import javax.inject.Inject;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.function.Function;

public class StandardCustomAuthSessionService implements CustomAuthSessionService {

    private ObjectMapper objectMapper;

    private AuthSchemeDao authSchemeDao;

    private CustomAuthUserDao customAuthUserDao;

    private CryptoKeyUtility cryptoKeyUtility;

    @Override
    public Session getSession(final String jwt) {

        final var decoded = new JWTCredentials(jwt);
        final var schemes = getAuthSchemeDao().getAuthSchemesByAudience(decoded.getAudience());

        return schemes
            .stream()
            .filter(scheme -> isValid(decoded, scheme))
            .map(scheme -> jwtToSession(decoded, scheme))
            .findFirst()
            .orElseThrow(ForbiddenException::new);

    }

    private boolean isValid(final JWTCredentials decoded, final AuthScheme scheme) {
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
                throw new InternalException("Unsupported algoirthm: " + scheme.getAlgorithm());
        }
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

    private boolean isValidECDSA(final JWTCredentials decoded,
                                 final AuthScheme scheme,
                                 final Function<ECPublicKey, Algorithm> algorithmFactory) {

        final var key = getCryptoKeyUtility().getPublicKey(
                scheme.getAlgorithm(),
                scheme.getPublicKey(),
                ECPublicKey.class
        );

        final var algo = algorithmFactory.apply(key);
        return decoded.verify(algo);

    }

    private Session jwtToSession(final JWTCredentials decoded, final AuthScheme scheme) {
        return null;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public AuthSchemeDao getAuthSchemeDao() {
        return authSchemeDao;
    }

    @Inject
    public void setAuthSchemeDao(AuthSchemeDao authSchemeDao) {
        this.authSchemeDao = authSchemeDao;
    }

    public CustomAuthUserDao getCustomAuthUserDao() {
        return customAuthUserDao;
    }

    @Inject
    public void setCustomAuthUserDao(CustomAuthUserDao customAuthUserDao) {
        this.customAuthUserDao = customAuthUserDao;
    }

    public CryptoKeyUtility getCryptoKeyUtility() {
        return cryptoKeyUtility;
    }

    @Inject
    public void setCryptoKeyUtility(CryptoKeyUtility cryptoKeyUtility) {
        this.cryptoKeyUtility = cryptoKeyUtility;
    }

}
