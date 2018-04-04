package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.friend.Friendship;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.List;

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = MongoFriendId.class,
        extractor = MongoFriendIdExtractor.class,
        processors = MongoFriendIdProcessor.class))
@SearchableDocument(fields = {
        @SearchableField(name = "userId", path = "/user/objectId", extractor = ObjectIdExtractor.class),
        @SearchableField(name = "name", path = "/friend/name"),
        @SearchableField(name = "friendship", path = "/friendship")
})
@Indexes({
        @Index(fields = @Field(value = "_id.user")),
        @Index(fields = @Field(value = "_id.friend")),
        @Index(fields = @Field(value = "user")),
        @Index(fields = @Field(value = "friend")),
})
public class MongoFriend {

    @Id
    private MongoFriendId objectId;

    @Reference
    private User user;

    @Reference
    private User friend;

    @Property
    private Timestamp creation;

    @Transient
    private Friendship friendship;

    @Transient
    private List<MongoProfile> profiles;

    public MongoFriendId getObjectId() {

        return objectId;
    }

    public void setObjectId(MongoFriendId objectId) {
        this.objectId = objectId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public Timestamp getCreation() {
        return creation;
    }

    public void setCreation(Timestamp creation) {
        this.creation = creation;
    }

    public Friendship getFriendship() {
        return friendship;
    }

    public void setFriendship(Friendship friendship) {
        this.friendship = friendship;
    }

    public List<MongoProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<MongoProfile> profiles) {
        this.profiles = profiles;
    }

}
