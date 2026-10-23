package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * The resolved claims of a verified signed machine token. Returned by
 * {@link dev.getelements.elements.sdk.service.auth.MachineTokenService#verify(String, String)}.
 *
 * <p>The standard claims are exposed as fields; {@link #getClaims()} contains exactly the
 * domain-specific claims the caller embedded at mint time.</p>
 */
@Schema(description = "The resolved claims of a verified machine token.")
public class MachineTokenClaims {

    /** Creates a new instance. */
    public MachineTokenClaims() {}

    @Schema(description = "The token id (jti claim).")
    private String tokenId;

    @Schema(description = "The subject (sub) the token asserts.")
    private String subject;

    @Schema(description = "The issuer (iss) of the token.")
    private String issuer;

    @Schema(description = "The audience (aud) the token was verified against.")
    private String audience;

    @Schema(description = "When the token was issued, in milliseconds since the epoch.")
    private long issuedAt;

    @Schema(description = "When the token expires, in milliseconds since the epoch.")
    private long expiry;

    @Schema(description = "The domain-specific claims embedded in the token.")
    private Map<String, Object> claims;

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, Object> claims) {
        this.claims = claims;
    }

}
