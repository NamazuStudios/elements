package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import com.namazustudios.socialengine.model.blockchain.Nep6Wallet;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import io.neow3j.wallet.nep6.NEP6Wallet;
import org.bson.types.ObjectId;

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@Entity(value = "neo_wallet", useDiscriminator = false)
@Indexes({
        @Index(fields = {@Field("user")}),
        @Index(fields = @Field(value = "displayName", type = IndexType.TEXT))
})
public class MongoNeoWallet {

    @Id
    public ObjectId id;

    @Indexed
    @Property
    public String displayName;

    @Property
    public Nep6Wallet wallet;

    @Reference
    public MongoUser user;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Nep6Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Nep6Wallet wallet) {
        this.wallet = wallet;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }
}
