package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Property;

import java.sql.Timestamp;

/**
 * MongoDB entity for a single outstanding provider authorization code. The {@code code} is the document
 * {@code _id}, so uniqueness is enforced by the primary key. The TTL index on {@code expiry} (with
 * {@code expireAfterSeconds=0}) automatically removes expired codes, mirroring {@link MongoOidcLoginAttempt}.
 */
@Entity(value = "oidc_authorization_code", useDiscriminator = false)
@Indexes(@Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = 0)))
public class MongoOidcAuthorizationCode {

    @Id
    private String code;

    @Property
    private String clientId;

    @Property
    private String userId;

    @Property
    private String applicationId;

    @Property
    private String redirectUri;

    @Property
    private String scope;

    @Property
    private String state;

    @Property
    private String nonce;

    @Property
    private long authTime;

    @Property
    private String codeChallenge;

    @Property
    private String codeChallengeMethod;

    @Property
    private OidcAuthorizationCode.Status status;

    @Property
    private Timestamp expiry;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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

    public long getAuthTime() {
        return authTime;
    }

    public void setAuthTime(long authTime) {
        this.authTime = authTime;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public OidcAuthorizationCode.Status getStatus() {
        return status;
    }

    public void setStatus(OidcAuthorizationCode.Status status) {
        this.status = status;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
