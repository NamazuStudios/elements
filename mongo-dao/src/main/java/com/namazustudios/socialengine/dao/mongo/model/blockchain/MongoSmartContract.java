package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Entity(value = "wallet")
@Indexes({
        @Index(fields = @Field("api")),
        @Index(fields = @Field("networks")),
        @Index(fields = {
                @Field("api"),
                @Field("networks")
        } )
})
public class MongoSmartContract {

    @Id
    private ObjectId objectId;

    @Property
    private String displayName;

    @Property
    private BlockchainApi api;

    @Property
    private List<BlockchainNetwork> networks;

    @Reference
    private MongoWallet wallet;

    @Property
    private Map<String, Object> metadata;

}
