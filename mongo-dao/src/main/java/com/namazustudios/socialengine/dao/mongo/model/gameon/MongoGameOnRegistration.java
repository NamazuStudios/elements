package com.namazustudios.socialengine.dao.mongo.model.gameon;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

@SearchableIdentity(@SearchableField(
    name = "id",
    path = "/objectId",
    type = ObjectId.class,
    extractor = ObjectIdExtractor.class,
    processors = ObjectIdProcessor.class))
@SearchableDocument(
    fields = {
        @SearchableField(name = "userId",      path = "/user/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class),
        @SearchableField(name = "profileId",   path = "/profile/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class),
        @SearchableField(name = "userName",    path = "/user/name"),
        @SearchableField(name = "userEmail",   path = "/user/email"),
        @SearchableField(name = "displayName", path = "/profile/displayName")
    })
@Entity(value = "game_on_registration", noClassnameStored = true)
public class MongoGameOnRegistration {

    @Id
    private ObjectId objectId;

    @Indexed
    @Reference
    private MongoUser user;

    @Indexed
    @Reference
    private MongoProfile profile;

    @Property
    private String playerToken;

    @Property
    @Indexed
    private String externalPlayerId;

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

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    public String getPlayerToken() {
        return playerToken;
    }

    public void setPlayerToken(String playerToken) {
        this.playerToken = playerToken;
    }

    public String getExternalPlayerId() {
        return externalPlayerId;
    }

    public void setExternalPlayerId(String externalPlayerId) {
        this.externalPlayerId = externalPlayerId;
    }

}
