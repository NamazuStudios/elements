package com.namazustudios.socialengine.dao.mongo.model.auth;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@Entity(value = "auth_scheme", useDiscriminator = false)
@SearchableDocument(fields = {
        @SearchableField(name = "audience", path = "/audience"),
        @SearchableField(name = "userLevel", path = "/userLevel")
})
public class MongoAuthScheme {

    @Id
    public String id;

    @Property
    public String audience;

    @Property
    public String pubKey;

    @Property
    public String userLevel;

    @Property
    public List<String> allowedIssuers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }
}
