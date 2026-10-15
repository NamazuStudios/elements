package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The resolved claims of an Elements-issued session token. This is the result of locally verifying a
 * session token's signature and standard claims, prior to consulting the server-side session record.
 */
@Schema(description = "The resolved claims of an Elements-issued session token.")
public class SessionTokenClaims {

    @Schema(description = "The id of the user to whom the token was issued.")
    private String subject;

    @Schema(description = "The server-side session id embedded in the token.")
    private String sessionId;

    @Schema(description = "The User.Level name of the user at issuance time.")
    private String level;

    @Schema(description = "The issuer of the token.")
    private String issuer;

    @Schema(description = "The audience of the token.")
    private String audience;

    @Schema(description = "When the token was issued, in milliseconds since the epoch.")
    private long issuedAt;

    @Schema(description = "When the token expires, in milliseconds since the epoch.")
    private long expiry;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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

}
