package com.namazustudios.socialengine.service.auth;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.namazustudios.socialengine.dao.AuthSchemeDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.auth.*;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class SuperUserAuthSchemeService implements AuthSchemeService {

    private AuthSchemeDao authSchemeDao;

    private CryptoKeyUtility cryptoKeyUtility;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<AuthScheme> getAuthSchemes(final int offset, final int count, final List<String> tags) {
        return getAuthSchemeDao().getAuthSchemes(offset, count, tags);
    }

    @Override
    public AuthScheme getAuthScheme(final String authSchemeId) {
        return getAuthSchemeDao().getAuthScheme(authSchemeId);
    }

    @Override
    public CreateAuthSchemeResponse createAuthScheme(final CreateAuthSchemeRequest authSchemeRequest) {

        getValidationHelper().validateModel(authSchemeRequest, Create.class);

        final var response = new CreateAuthSchemeResponse();

        final var authScheme = new AuthScheme();
        authScheme.setTags(authSchemeRequest.getTags());
        authScheme.setAlgorithm(authSchemeRequest.getAlgorithm());
        authScheme.setAudience(authSchemeRequest.getAudience());
        authScheme.setUserLevel(authSchemeRequest.getUserLevel());
        authScheme.setAllowedIssuers(authSchemeRequest.getAllowedIssuers());

        if (authSchemeRequest.getPublicKey() == null) {
            final var keyPair = getJwtCryptoUtility().generateKeyPair(authSchemeRequest.getAlgorithm());
            response.setPublicKey(keyPair.getPublicKeyBase64());
            response.setPrivateKey(keyPair.getPrivateKeyBase64());
            authScheme.setPublicKey(keyPair.getPublicKeyBase64());
        } else {

            final var publicKey = getJwtCryptoUtility().getPublicKey(
                    authSchemeRequest.getAlgorithm(),
                    authSchemeRequest.getPublicKey()
            );

            final var encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            response.setPublicKey(encoded);
            authScheme.setPublicKey(encoded);

        }

        final var result = getAuthSchemeDao().createAuthScheme(authScheme);
        response.setScheme(result);

        return response;

    }

    @Override
    public UpdateAuthSchemeResponse updateAuthScheme(final String authSchemeId,
                                                     final UpdateAuthSchemeRequest authSchemeRequest) {

        getValidationHelper().validateModel(authSchemeRequest, Update.class);

        final var response = new UpdateAuthSchemeResponse();
        final var authScheme = getAuthSchemeDao().getAuthScheme(authSchemeId);

        authScheme.setTags(authSchemeRequest.getTags());
        authScheme.setAlgorithm(authSchemeRequest.getAlgorithm());
        authScheme.setAudience(authSchemeRequest.getAudience());
        authScheme.setUserLevel(authSchemeRequest.getUserLevel());
        authScheme.setAllowedIssuers(authSchemeRequest.getAllowedIssuers());

        if (authSchemeRequest.isRegenerate()) {

            if (authSchemeRequest.getPublicKey() != null) {
                throw new BadRequestException("Cannot specify both a public key and regeneration in the same request.");
            }

            final var keyPair = getJwtCryptoUtility().generateKeyPair(authSchemeRequest.getAlgorithm());
            response.setPublicKey(keyPair.getPublicKeyBase64());
            response.setPrivateKey(keyPair.getPrivateKeyBase64());
            authScheme.setPublicKey(keyPair.getPublicKeyBase64());

        } else if (authSchemeRequest.getPublicKey() != null) {

            final var publicKey = getJwtCryptoUtility().getPublicKey(
                authSchemeRequest.getAlgorithm(),
                authSchemeRequest.getPublicKey()
            );

            final var encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            response.setPublicKey(encoded);
            authScheme.setPublicKey(encoded);

        }

        final var authSchemeResult = getAuthSchemeDao().updateAuthScheme(authScheme);
        response.setScheme(authSchemeResult);
        response.setPublicKey(authSchemeResult.getPublicKey());

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

    public static void main(String[] args) throws Exception {

//        final var util = new StandardCryptoKeyUtility();
//
//        final var kpRSA = util.generateKeyPair(AuthSchemeAlgorithm.RSA_512);
//        final var kpECDSA = util.generateKeyPair(AuthSchemeAlgorithm.ECDSA_512);
//
//        final var kfRSA = KeyFactory.getInstance("RSA");
//        final var kfECDSA = KeyFactory.getInstance("EC");
//
//        final var rsaPub = kfRSA.generatePublic(kpRSA.getPublicKey());
//        final var ecdsaPub = kfECDSA.generatePublic(kpECDSA.getPublicKey());
//
//        rsaPub.getAlgorithm();
//        ecdsaPub.getAlgorithm();
//
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
