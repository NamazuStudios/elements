package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * A freshly minted signed machine token and the claims it carries. Returned by
 * {@link dev.getelements.elements.sdk.service.auth.MachineTokenService#issue(MachineTokenRequest)}.
 *
 * <p>The {@link #getToken() token} is the compact JOSE serialization to hand to the caller. The
 * {@link #getTokenId() token id} is the {@code jti} claim, which is what
 * {@link dev.getelements.elements.sdk.service.auth.MachineTokenService#revoke(String, long)} accepts.</p>
 */
@Schema(description = "A minted machine token and its decoded claims.")
public class MachineToken {

    /** Creates a new instance. */
    public MachineToken() {}

    @Schema(description = "The signed token, in compact JOSE serialization.")
    private String token;

    @Schema(description = "The token id (jti claim), used for revocation.")
    private String tokenId;

    @Schema(description = "The subject (sub) the token asserts.")
    private String subject;

    @Schema(description = "The issuer (iss) of the token.")
    private String issuer;

    @Schema(description = "The audience (aud) of the token.")
    private String audience;

    @Schema(description = "When the token was issued, in milliseconds since the epoch.")
    private long issuedAt;

    @Schema(description = "When the token expires, in milliseconds since the epoch.")
    private long expiry;

    @Schema(description = "The custom claims embedded in the token.")
    private Map<String, Object> claims;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

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
