package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

@Entity(value = "vault", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("user"))
})
public class MongoVault {

    @Id
    private ObjectId objectId;

    @Reference
    private MongoUser user;

    @Property
    private String displayName;

    @Property
    private MongoVaultKey key;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public MongoVaultKey getKey() {
        return key;
    }

    public void setKey(MongoVaultKey key) {
        this.key = key;
    }

}
