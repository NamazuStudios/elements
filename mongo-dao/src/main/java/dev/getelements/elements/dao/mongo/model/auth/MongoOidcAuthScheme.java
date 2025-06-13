package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.JWKSet;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Objects;

@Entity(value = "oidc_auth_scheme", useDiscriminator = false)
@Indexes({
        @Index(fields = {
                @Field("name"),
                @Field("issuer")
        }, options = @IndexOptions(unique = true))
})
public class MongoOidcAuthScheme {

    @Id
    private ObjectId id;

    @Property
    private String name;

    @Property
    private String issuer;

    @Property
    private List<JWK> keys;

    @Property
    private String keysUrl;

    @Property
    private String mediaType = "application/json";

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public List<JWK> getKeys() {
        return keys;
    }

    public void setKeys(List<JWK> keys) {
        this.keys = keys;
    }

    public String getKeysUrl() {
        return keysUrl;
    }

    public void setKeysUrl(String keysUrl) {
        this.keysUrl = keysUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoOidcAuthScheme that = (MongoOidcAuthScheme) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getKeys(), that.getKeys()) && Objects.equals(getIssuer(), that.getIssuer()) && Objects.equals(getKeysUrl(), that.getKeysUrl()) && Objects.equals(getMediaType(), that.getMediaType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getIssuer(), getKeys(), getKeysUrl(), getMediaType());
    }

}

