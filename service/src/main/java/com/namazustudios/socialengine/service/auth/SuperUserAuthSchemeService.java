package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.AuthSchemeDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.auth.*;
import com.namazustudios.socialengine.rt.exception.BadRequestException;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

public class SuperUserAuthSchemeService implements AuthSchemeService {

    private AuthSchemeDao authSchemeDao;

    private CryptoKeyUtility cryptoKeyUtility;

    @Override
    public Pagination<AuthScheme> getAuthSchemes(final int offset, final int count, final List<String> tags) {
        return getAuthSchemeDao().getAuthSchemes(offset, count, tags);
    }

    @Override
    public AuthScheme getAuthScheme(final String authSchemeId) {
        return getAuthSchemeDao().getAuthScheme(authSchemeId);
    }

    @Override
    public UpdateAuthSchemeResponse updateAuthScheme(final String authSchemeId,
                                                     final UpdateAuthSchemeRequest authSchemeRequest) {

        final var response = new UpdateAuthSchemeResponse();
        final var authScheme = getAuthSchemeDao().getAuthScheme(authSchemeId);
        authScheme.setAudience(authSchemeRequest.getAudience());
        authScheme.setUserLevel(authSchemeRequest.getUserLevel());
        authScheme.setAllowedIssuers(authSchemeRequest.getAllowedIssuers());

        if (authSchemeRequest.isRegenerate()) {

            if (authSchemeRequest.getPubKey() != null) {
                throw new BadRequestException("Cannot specify both a public key and regeneration in the same request.");
            }

            final var keyPair = getJwtCryptoUtility().generateKeyPair(authSchemeRequest.getAlgorithm());
            response.setPublicKey(keyPair.getPublicKeyBase64());
            response.setPrivateKey(keyPair.getPrivateKeyBase64());
            authScheme.setPublicKey(keyPair.getPublicKeyBase64());

        } else if (authScheme.getPublicKey() != null) {
            authScheme.setPublicKey(authScheme.getPublicKey());
        }

        final var authSchemeResult = getAuthSchemeDao().updateAuthScheme(authScheme);
        response.setScheme(authSchemeResult);
        response.setPublicKey(authSchemeResult.getPublicKey());

        return response;

    }

    @Override
    public CreateAuthSchemeResponse createAuthScheme(final CreateAuthSchemeRequest authSchemeRequest) {

        final var response = new CreateAuthSchemeResponse();

        final var authScheme = new AuthScheme();
        authScheme.setAudience(authSchemeRequest.getAudience());
        authScheme.setUserLevel(authSchemeRequest.getUserLevel());

        if (authSchemeRequest.getPublicKey() == null) {
            final var keyPair = getJwtCryptoUtility().generateKeyPair(authSchemeRequest.getAlgorithm());
            response.setPublicKey(keyPair.getPublicKeyBase64());
            response.setPrivateKey(keyPair.getPrivateKeyBase64());
            authScheme.setPublicKey(keyPair.getPublicKeyBase64());
        } else {
            getJwtCryptoUtility().getPublicKey(
                authScheme.getAlgorithm(),
                authScheme.getPublicKey()
            );
        }

        final var result = getAuthSchemeDao().createAuthScheme(authScheme);
        response.setScheme(result);


        return response;

    }

    @Override
    public void deleteAuthScheme(String authSchemeId) {
        getAuthSchemeDao().deleteAuthScheme(authSchemeId);
    }

    public AuthSchemeDao getAuthSchemeDao() {
        return authSchemeDao;
    }

    @Inject
    public void setAuthSchemeDao(AuthSchemeDao authSchemeDao) {
        this.authSchemeDao = authSchemeDao;
    }

    public CryptoKeyUtility getJwtCryptoUtility() {
        return cryptoKeyUtility;
    }

    @Inject
    public void setJwtCryptoUtility(CryptoKeyUtility cryptoKeyUtility) {
        this.cryptoKeyUtility = cryptoKeyUtility;
    }

    public static void main(String[] args) throws Exception {

        final var util = new StandardCryptoKeyUtility();

        final var kpRSA = util.generateKeyPair(AuthSchemeAlgorithm.RSA_512);
        final var kpECDSA = util.generateKeyPair(AuthSchemeAlgorithm.ECDSA_512);

        final var kfRSA = KeyFactory.getInstance("RSA");
        final var kfECDSA = KeyFactory.getInstance("EC");

        final var rsaPub = kfRSA.generatePublic(kpRSA.getPublicKey());
        final var ecdsaPub = kfECDSA.generatePublic(kpECDSA.getPublicKey());

        rsaPub.getAlgorithm();
        ecdsaPub.getAlgorithm();

//        final var util = new StandardCryptoKeyUtility();
//
//        final var kpRSA = util.generateKeyPair(AuthSchemeAlgorithm.RSA_512);
//        final var kpECDSA = util.generateKeyPair(AuthSchemeAlgorithm.ECDSA_512);
//
//        final var signAlgoRSA = Algorithm.RSA512(
//            (RSAPublicKey) kpRSA.getPublic(),
//            (RSAPrivateKey) kpRSA.getPrivate()
//        );
//
//        final var signAlgoECDSA = Algorithm.ECDSA512(
//            (ECPublicKey) kpECDSA.getPublic(),
//            (ECPrivateKey) kpECDSA.getPrivate()
//        );
//
//        System.out.println("");
//        System.out.println(new String(kpRSA.getPublic().getEncoded()));
//
//        final var jwtRSA = JWT.create()
//            .withAudience("test")
//            .withClaim("test", "test")
//            .sign(signAlgoRSA);
//
//        final var jwtECDSA = JWT.create()
//            .withAudience("test")
//            .withClaim("test", "test")
//            .sign(signAlgoECDSA);
//
//        final var kfRSA = KeyFactory.getInstance("RSA");
//        final var kfECDSA = KeyFactory.getInstance("EC");
//
//        final var x509RSA = new X509EncodedKeySpec(kpRSA.getPublic().getEncoded());
//        final var x509ECDSA = new X509EncodedKeySpec(kpECDSA.getPublic().getEncoded());
//
//        final var keySpecRSA = kfRSA.generatePublic(x509RSA);
//        final var keySpecECDSA = kfECDSA.generatePublic(x509ECDSA);
//
//        final var verifyAlgoRSA = Algorithm.RSA512((RSAPublicKey) keySpecRSA, null);
//        final var verifyAlgoECDSA = Algorithm.ECDSA512((ECPublicKey) keySpecECDSA, null);
//
//        final var decodedRSA = JWT
//            .require(verifyAlgoRSA)
//            .withAudience("test")
//            .build()
//            .verify(jwtRSA);
//
//        final var decodedECDSA = JWT
//            .require(verifyAlgoECDSA)
//            .withAudience("test")
//            .build()
//            .verify(jwtECDSA);
//
//        System.out.println("RSA: " + decodedRSA);
//        System.out.println("Eliptic Curve:" + decodedECDSA);

    }

}
