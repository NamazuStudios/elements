package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import com.namazustudios.socialengine.model.blockchain.Ownership;
import com.namazustudios.socialengine.model.blockchain.Token;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

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
        @SearchableField(name = "type", path = "/type")
})
@Indexes({
        @Index(fields = @Field(value = "name", type = IndexType.TEXT))
})
public class MongoNeoToken {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

    @Property
    private List<String> tags;

    @Property
    private String type;

    @Property
    private Token token;

    @Property
    private Map<String, Object> metadata;

    @Property
    private String contract;

    @Property
    private boolean listed;

    @Property
    private boolean minted;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetaData(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public boolean isListed() {
        return listed;
    }

    public void setListed(boolean listed) {
        this.listed = listed;
    }

    public boolean isMinted() {
        return minted;
    }

    public void setMinted(boolean minted) {
        this.minted = minted;
    }
}
