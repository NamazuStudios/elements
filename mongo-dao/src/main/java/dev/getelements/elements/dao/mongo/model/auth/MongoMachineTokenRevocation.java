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
 * MongoDB entity for a revoked machine-token id. The {@code _id} is the token's {@code jti}; the TTL index
 * on {@code expiry} removes the denylist entry once the token can no longer verify anyway.
 */
@Entity(value = "machine_token_revocation", useDiscriminator = false)
@Indexes(@Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = 0)))
public class MongoMachineTokenRevocation {

    @Id
    private String id;

    @Property
    private Timestamp expiry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
