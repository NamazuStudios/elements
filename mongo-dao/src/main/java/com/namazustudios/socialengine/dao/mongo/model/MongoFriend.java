package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Reference;

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = MongoFriendId.class,
        extractor = MongoFriendIdExtractor.class,
        processors = MongoFriendIdProcessor.class))
@SearchableDocument(fields = {
        @SearchableField(name = "name", path = "/name"),
        @SearchableField(name = "email", path = "/email"),
        @SearchableField(name = "active", path = "/active"),
        @SearchableField(name = "level", path = "/level"),
        @SearchableField(name = "facebookId", path = "/facebookId")
})
public class MongoFriend {

    @Id
    private MongoFriendId objectId;

    @Indexed
    @Reference
    private MongoUser user;

    @Indexed
    @Reference
    private MongoUser friend;

    public MongoFriendId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoFriendId objectId) {
        this.objectId = objectId;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public MongoUser getFriend() {
        return friend;
    }

    public void setFriend(MongoUser friend) {
        this.friend = friend;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoFriend)) return false;

        MongoFriend that = (MongoFriend) o;

        if (getObjectId() != null ? !getObjectId().equals(that.getObjectId()) : that.getObjectId() != null)
            return false;
        if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null) return false;
        return getFriend() != null ? getFriend().equals(that.getFriend()) : that.getFriend() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getFriend() != null ? getFriend().hashCode() : 0);
        return result;
    }

}
