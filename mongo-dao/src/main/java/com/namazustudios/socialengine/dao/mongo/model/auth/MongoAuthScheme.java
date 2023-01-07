package com.namazustudios.socialengine.dao.mongo.model.auth;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;
import com.namazustudios.socialengine.model.user.User;
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
@Indexes({
    @Index(fields = @Field("tags")),
    @Index(fields = @Field("audience"), options = @IndexOptions(unique = true))
})
@SearchableDocument(fields = {
        @SearchableField(name = "audience", path = "/audience"),
        @SearchableField(name = "userLevel", path = "/userLevel")
})
public class MongoAuthScheme {

    @Id
    private ObjectId id;

    @Property
    private String audience;

    @Property
    private String publicKey;

    @Property
    private PrivateKeyCrytpoAlgorithm algorithm;

    @Property
    private User.Level userLevel;

    @Property
    private List<String> tags;

    @Property
    private List<String> allowedIssuers;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public List<String> getAllowedIssuers() {
        return allowedIssuers;
    }

    public void setAllowedIssuers(List<String> allowedIssuers) {
        this.allowedIssuers = allowedIssuers;
    }

    public User.Level getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(User.Level userLevel) {
        this.userLevel = userLevel;
    }

}
