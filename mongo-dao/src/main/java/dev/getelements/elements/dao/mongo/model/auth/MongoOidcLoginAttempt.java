package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.OidcLoginAttemptStatus;
import dev.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * MongoDB entity for a single pending browser-redirect OIDC login attempt.
 * The {@code id} field is the document {@code _id}.
 *
 * <p>The TTL index on {@code expiry} (with {@code expireAfterSeconds=0}) causes MongoDB to automatically
 * remove expired attempts, mirroring {@code MongoPasswordResetToken}.
 */
@Entity(value = "oidc_login_attempt", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("state"), options = @IndexOptions(unique = true)),
        @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = 0))
})
public class MongoOidcLoginAttempt {

    @Id
    private String id;

    @Property
    private String provider;

    @Property
    private String state;

    @Property
    private String nonce;

    @Property
    private OidcLoginAttemptStatus status;

    @Property
    private String sessionToken;

    @Property
    private String failureReason;

    @Property
    private Timestamp expiry;

    @Property
    private String successRedirectUrl;

    @Property
    private String errorRedirectUrl;

    @Property
    private String linkedUserId;

    @Property
    private String applicationNameOrId;

    @Property
    private String linkClaimsJson;

    @Property
    private String confirmToken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public OidcLoginAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(OidcLoginAttemptStatus status) {
        this.status = status;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    public String getSuccessRedirectUrl() {
        return successRedirectUrl;
    }

    public void setSuccessRedirectUrl(String successRedirectUrl) {
        this.successRedirectUrl = successRedirectUrl;
    }

    public String getErrorRedirectUrl() {
        return errorRedirectUrl;
    }

    public void setErrorRedirectUrl(String errorRedirectUrl) {
        this.errorRedirectUrl = errorRedirectUrl;
    }

    public String getLinkedUserId() {
        return linkedUserId;
    }

    public void setLinkedUserId(String linkedUserId) {
        this.linkedUserId = linkedUserId;
    }

    public String getApplicationNameOrId() {
        return applicationNameOrId;
    }

    public void setApplicationNameOrId(String applicationNameOrId) {
        this.applicationNameOrId = applicationNameOrId;
    }

    public String getLinkClaimsJson() {
        return linkClaimsJson;
    }

    public void setLinkClaimsJson(String linkClaimsJson) {
        this.linkClaimsJson = linkClaimsJson;
    }

    public String getConfirmToken() {
        return confirmToken;
    }

    public void setConfirmToken(String confirmToken) {
        this.confirmToken = confirmToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoOidcLoginAttempt that = (MongoOidcLoginAttempt) o;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getProvider(), that.getProvider())
                && Objects.equals(getState(), that.getState())
                && Objects.equals(getNonce(), that.getNonce())
                && getStatus() == that.getStatus()
                && Objects.equals(getSessionToken(), that.getSessionToken())
                && Objects.equals(getFailureReason(), that.getFailureReason())
                && Objects.equals(getExpiry(), that.getExpiry())
                && Objects.equals(getSuccessRedirectUrl(), that.getSuccessRedirectUrl())
                && Objects.equals(getErrorRedirectUrl(), that.getErrorRedirectUrl())
                && Objects.equals(getLinkedUserId(), that.getLinkedUserId())
                && Objects.equals(getApplicationNameOrId(), that.getApplicationNameOrId())
                && Objects.equals(getLinkClaimsJson(), that.getLinkClaimsJson())
                && Objects.equals(getConfirmToken(), that.getConfirmToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProvider(), getState(), getNonce(), getStatus(),
                getSessionToken(), getFailureReason(), getExpiry(), getSuccessRedirectUrl(), getErrorRedirectUrl(),
                getLinkedUserId(), getApplicationNameOrId(), getLinkClaimsJson(), getConfirmToken());
    }

}
