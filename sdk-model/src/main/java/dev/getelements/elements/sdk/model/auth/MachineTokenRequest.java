package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.Map;

/**
 * Parameters for minting a signed machine token through
 * {@link dev.getelements.elements.sdk.service.auth.MachineTokenService}.
 *
 * <p>A machine token is a deployment-signed JWT for a non-user principal (a service, worker, or
 * long-lived process). The caller supplies the subject, audience, lifetime, and any domain-specific
 * claims it needs; the service adds the standard JOSE claims ({@code iss}, {@code sub}, {@code aud},
 * {@code iat}, {@code exp}, {@code jti}) and signs the result with the deployment's OIDC signing key.
 * The token is therefore verifiable by any party that trusts the published JWKS.</p>
 *
 * <p>Custom claims are restricted to JSON-safe values (strings, numbers, booleans, collections, and
 * string-keyed maps). Standard claims cannot be overridden through {@link #getClaims()}.</p>
 */
@Schema(description = "Parameters for minting a signed machine token.")
public class MachineTokenRequest {

    /** Creates a new instance. */
    public MachineTokenRequest() {}

    @Schema(description = "The subject (sub) the token asserts, typically a service or user id.")
    @NotBlank
    private String subject;

    @Schema(description = "The audience (aud) the token is issued for. Verification must present the same value.")
    @NotBlank
    private String audience;

    @Schema(description = "The token lifetime, in seconds. Must be positive.")
    @Positive
    private long ttlSeconds;

    @Schema(description = "Optional caller-supplied token id (jti). Generated when omitted. Must not already " +
            "be revoked.")
    private String tokenId;

    @Schema(description = "Optional domain-specific claims to embed in the token. Standard claims " +
            "(iss, sub, aud, exp, nbf, iat, jti, and the reserved Elements claims) are rejected.")
    private Map<String, Object> claims;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, Object> claims) {
        this.claims = claims;
    }

}
