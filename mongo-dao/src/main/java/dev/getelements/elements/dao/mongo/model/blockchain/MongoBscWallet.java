package dev.getelements.elements.dao.mongo.model.blockchain;

import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.dao.mongo.model.ObjectIdExtractor;
import dev.getelements.elements.dao.mongo.model.ObjectIdProcessor;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@Entity(value = "bsc_wallet", useDiscriminator = false)
@Indexes({
        @Index(fields = {@Field("user")}),
        @Index(fields = @Field(value = "displayName", type = IndexType.TEXT))
})
public class MongoBscWallet {

    @Id
    public ObjectId objectId;

    @Property
    public String displayName;

    @Property
    public byte[] wallet;

    @Reference
    public MongoUser user;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getWallet() {
        return wallet;
    }

    public void setWallet(byte[] wallet) {
        this.wallet = wallet;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

}
