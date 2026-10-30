package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.dao.MachineTokenRevocationDao;
import dev.getelements.elements.sdk.model.auth.JwtSigningKey;
import dev.getelements.elements.sdk.model.auth.MachineTokenRequest;
import dev.getelements.elements.sdk.model.exception.ConflictException;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class MachineTokenServiceTest {

    private static final String ISSUER = "https://elements.test/api/rest";
    private static final String AUDIENCE = "namazu-cloud";
    private static final String KID = "kid-1";

    private static final String SUBJECT = "user-1";

    private MachineTokenRevocationDao revocationDao;

    private StandardMachineTokenService service;

    @BeforeMethod
    public void setup() throws Exception {

        revocationDao = mock(MachineTokenRevocationDao.class);

        final var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();
        final var publicKey = (RSAPublicKey) keyPair.getPublic();
        final var privateKey = (RSAPrivateKey) keyPair.getPrivate();

        final var signingKey = new JwtSigningKey();
        signingKey.setKid(KID);

        final var keySupport = mock(StandardSessionTokenKeySupport.class);
        when(keySupport.getActiveSigningKey()).thenReturn(signingKey);
        when(keySupport.getPrivateKey(any())).thenReturn(privateKey);
        when(keySupport.getPublicKey(KID)).thenReturn(publicKey);

        service = new StandardMachineTokenService();
        service.setMachineTokenRevocationDao(revocationDao);
        service.setSessionTokenKeySupport(keySupport);
        service.setIssuer(ISSUER);

    }

    @Test
    public void issueShouldEmbedStandardAndCustomClaims() {

        final var request = request(300, Map.of(
                "org", "org-1",
                "instance", "pod-1",
                "capabilities", List.of("MINT_AGENT_CREDENTIAL")));

        final var minted = service.issue(request);

        assertNotNull(minted.getToken());
        assertNotNull(minted.getTokenId());
        assertEquals(minted.getSubject(), SUBJECT);
        assertEquals(minted.getIssuer(), ISSUER);
        assertEquals(minted.getAudience(), AUDIENCE);
        assertEquals(minted.getExpiry(), minted.getIssuedAt() + 300_000);

        final var verified = service.verify(minted.getToken(), AUDIENCE);

        assertEquals(verified.getTokenId(), minted.getTokenId());
        assertEquals(verified.getSubject(), SUBJECT);
        assertEquals(verified.getIssuer(), ISSUER);
        assertEquals(verified.getAudience(), AUDIENCE);
        assertEquals(verified.getClaims().get("org"), "org-1");
        assertEquals(verified.getClaims().get("instance"), "pod-1");
        assertEquals(verified.getClaims().get("capabilities"), List.of("MINT_AGENT_CREDENTIAL"));
        assertFalse(verified.getClaims().containsKey("jti"));
        assertFalse(verified.getClaims().containsKey("iss"));

    }

    @Test
    public void issueShouldNormalizeNestedClaims() {

        final var request = request(300, Map.of(
                "meta", Map.of("owner", "user-1"),
                "roles", List.of("agent", "dispatcher")));

        final var verified = service.verify(service.issue(request).getToken(), AUDIENCE);

        assertEquals(verified.getClaims().get("meta"), Map.of("owner", "user-1"));
        assertEquals(verified.getClaims().get("roles"), List.of("agent", "dispatcher"));

    }

    @Test
    public void issueShouldGenerateDistinctTokenIdsWhenOmitted() {

        final var first = service.issue(request(300, Map.of()));
        final var second = service.issue(request(300, Map.of()));

        assertNotNull(first.getTokenId());
        assertNotEquals(first.getTokenId(), second.getTokenId());

    }

    @Test
    public void issueShouldHonorSuppliedTokenId() {

        final var request = request(300, Map.of());
        request.setTokenId("job-42");

        assertEquals(service.issue(request).getTokenId(), "job-42");
        verify(revocationDao).isRevoked("job-42");

    }

    @Test
    public void issueShouldRejectSuppliedRevokedTokenId() {

        final var request = request(300, Map.of());
        request.setTokenId("job-42");

        when(revocationDao.isRevoked("job-42")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.issue(request));

    }

    @Test
    public void issueShouldRejectReservedClaim() {

        assertThrows(InvalidParameterException.class,
                () -> service.issue(request(300, Map.of("aud", "somewhere-else"))));

    }

    @Test
    public void issueShouldRejectUnsupportedClaimValue() {

        assertThrows(InvalidParameterException.class,
                () -> service.issue(request(300, Map.of("broken", new Object()))));

    }

    @Test
    public void issueShouldRejectBlankSubject() {

        final var request = request(300, Map.of());
        request.setSubject(" ");

        assertThrows(InvalidParameterException.class, () -> service.issue(request));

    }

    @Test
    public void issueShouldRejectNonPositiveTtl() {

        assertThrows(InvalidParameterException.class, () -> service.issue(request(0, Map.of())));

    }

    @Test
    public void verifyShouldRejectWrongAudience() {

        final var minted = service.issue(request(300, Map.of()));

        assertThrows(UnauthorizedException.class, () -> service.verify(minted.getToken(), "other"));

    }

    @Test
    public void verifyShouldRejectTamperedToken() {

        final var minted = service.issue(request(300, Map.of()));
        final var parts = minted.getToken().split("\\.");
        final var signature = parts[2];
        final var first = signature.charAt(0) == 'A' ? 'B' : 'A';
        final var tampered = parts[0] + "." + parts[1] + "." + first + signature.substring(1);

        assertThrows(UnauthorizedException.class, () -> service.verify(tampered, AUDIENCE));

    }

    @Test
    public void verifyShouldRejectExpiredToken() throws Exception {

        final var minted = service.issue(request(1, Map.of()));

        Thread.sleep(1_500);

        assertThrows(UnauthorizedException.class, () -> service.verify(minted.getToken(), AUDIENCE));

    }

    @Test
    public void verifyShouldRejectRevokedToken() {

        final var minted = service.issue(request(300, Map.of()));

        when(revocationDao.isRevoked(minted.getTokenId())).thenReturn(true);

        assertThrows(UnauthorizedException.class, () -> service.verify(minted.getToken(), AUDIENCE));

    }

    @Test
    public void verifyShouldRejectBlankAudienceArgument() {

        final var minted = service.issue(request(300, Map.of()));

        assertThrows(InvalidParameterException.class, () -> service.verify(minted.getToken(), " "));

    }

    @Test
    public void revokeShouldDenylistUntilTokenExpiry() {

        final var expiry = System.currentTimeMillis() + 60_000;

        service.revoke("jti-1", expiry);

        verify(revocationDao).revoke("jti-1", new Timestamp(expiry));

    }

    @Test
    public void revokeShouldIgnoreAlreadExpiredToken() {

        service.revoke("jti-1", System.currentTimeMillis() - 1);

        verify(revocationDao, never()).revoke(anyString(), any());

    }

    @Test
    public void revokeShouldRejectBlankTokenId() {

        assertThrows(InvalidParameterException.class, () -> service.revoke(" ", System.currentTimeMillis()));

    }

    @Test
    public void tokenShouldBeVerifiableJwt() throws Exception {

        final var minted = service.issue(request(300, Map.of()));
        final var parsed = com.nimbusds.jwt.SignedJWT.parse(minted.getToken());
        final var claims = parsed.getJWTClaimsSet();

        assertTrue(parsed.getHeader().getKeyID().equals(KID));
        assertEquals(claims.getSubject(), SUBJECT);
        assertEquals(claims.getIssuer(), ISSUER);
        assertEquals(claims.getAudience(), List.of(AUDIENCE));
        assertEquals(claims.getJWTID(), minted.getTokenId());

    }

    private MachineTokenRequest request(final long ttlSeconds, final Map<String, Object> claims) {
        final var request = new MachineTokenRequest();
        request.setSubject(SUBJECT);
        request.setAudience(AUDIENCE);
        request.setTtlSeconds(ttlSeconds);
        request.setClaims(claims);
        return request;
    }

}
