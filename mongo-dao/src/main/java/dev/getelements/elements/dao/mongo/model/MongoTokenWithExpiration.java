package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

import java.sql.Timestamp;

@Entity("token_with_expiration")
@Indexes({
        @Index(fields = @Field("user")),
        @Index(fields = @Field(value = "expiry"))
}
)
public class MongoTokenWithExpiration {
    @Id
    private ObjectId objectId;
    @Reference
    private MongoUser user;

    @Property
    private Timestamp expiry;

    public MongoTokenWithExpiration(MongoUser user, Timestamp expiry) {
        this.user = user;
        this.expiry = expiry;
    }

    public MongoTokenWithExpiration() {
    }

    public ObjectId getId() {
        return this.objectId;
    }

    public MongoUser getUser() {
        return user;
    }


    public Timestamp getExpiry() {
        return expiry;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }
}
