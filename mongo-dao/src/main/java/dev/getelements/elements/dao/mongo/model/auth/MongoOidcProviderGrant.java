package dev.getelements.elements.dao.mongo.model.auth;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Property;

import java.sql.Timestamp;

/**
 * MongoDB entity for a provider grant: the record of a downstream client's delegated access to a user,
 * anchored to the Elements session currently backing its access token. The {@code sessionId} is uniquely
 * indexed for userinfo lookups; the TTL index on {@code expiry} removes finished grants.
 */
@Entity(value = "oidc_provider_grant", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("sessionId"), options = @IndexOptions(unique = true)),
        @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = 0))
})
public class MongoOidcProviderGrant {

    @Id
    private String id;

    @Property
    private String clientId;

    @Property
    private String userId;

    @Property
    private String applicationId;

    @Property
    private String scope;

    @Property
    private String sessionId;

    @Property
    private Timestamp expiry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
