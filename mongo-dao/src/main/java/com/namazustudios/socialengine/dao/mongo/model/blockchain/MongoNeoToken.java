package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import com.namazustudios.socialengine.model.blockchain.Token;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

import java.util.List;

@SearchableIdentity(@SearchableField(
        name = "objectId",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@Entity(value = "token", useDiscriminator = false)
@SearchableDocument(fields = {
        @SearchableField(name = "name", path = "/name"),
        @SearchableField(name = "tags", path = "/tags"),
        @SearchableField(name = "tokenUUID", path ="/tokenUUID")
})
@Indexes({
        @Index(fields = @Field(value = "name", type = IndexType.TEXT), options = @IndexOptions(unique = true))
})
public class MongoNeoToken {

    @Id
    private ObjectId objectId;

    @Property
    private String tokenUUID;

    @Property
    private String name;

    @Property
    private List<String> tags;

    @Property
    private Token token;

    @Property
    private boolean listed;

    @Property
    private BlockchainConstants.MintStatus mintStatus;

    @Property
    private String contractId;

    @Property
    private String seriesId;

    @Property
    private long totalMintedQuantity;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getTokenUUID() {
        return tokenUUID;
    }

    public void setTokenUUID(String tokenUUID) {
        this.tokenUUID = tokenUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public boolean isListed() {
        return listed;
    }

    public void setListed(boolean listed) {
        this.listed = listed;
    }

    public BlockchainConstants.MintStatus getMintStatus() {
        return mintStatus;
    }

    public void setMintStatus(BlockchainConstants.MintStatus mintStatus) {
        this.mintStatus = mintStatus;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public long getTotalMintedQuantity() {
        return totalMintedQuantity;
    }

    public void setTotalMintedQuantity(long totalMintedQuantity) {
        this.totalMintedQuantity = totalMintedQuantity;
    }
}
