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
        name = "id",
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
    public String id;

    @Property
    public String name;

    @Property
    public List<String> tags;

    @Property
    public String type;

    @Property
    public Token token;

    @Property
    public Map<String, Object> metaData;

    @Property
    private String contract;

    @Property
    private boolean listed;

    @Property
    private boolean minted;
}
