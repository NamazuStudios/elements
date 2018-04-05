package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.friend.Friendship;
import org.mongodb.morphia.annotations.*;

import java.util.List;
import java.util.Set;

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = MongoFriendshipId.class,
        extractor = MongoFriendIdExtractor.class,
        processors = MongoFriendIdProcessor.class))
@SearchableDocument(fields = {
    @SearchableField(name = "user", path = "/objectId/lesser"),
    @SearchableField(name = "user", path = "/objectId/greater")
})
@Indexes({
    @Index(fields = @Field("_id.lesser")),
    @Index(fields = @Field("_id.greater")),
    @Index(fields = @Field("lesserAccepted")),
    @Index(fields = @Field("greaterAccepted"))
})
public class MongoFriendship {

    @Id
    private MongoFriendshipId objectId;

    @Property
    private boolean lesserAccepted;

    @Property
    private boolean greaterAccepted;

    @Transient
    private Friendship friendship;

    @Transient
    private List<MongoProfile> profiles;

    public MongoFriendshipId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoFriendshipId objectId) {
        this.objectId = objectId;
    }

    public boolean isLesserAccepted() {
        return lesserAccepted;
    }

    public void setLesserAccepted(boolean lesserAccepted) {
        this.lesserAccepted = lesserAccepted;
    }

    public boolean isGreaterAccepted() {
        return greaterAccepted;
    }

    public void setGreaterAccepted(boolean greaterAccepted) {
        this.greaterAccepted = greaterAccepted;
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

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof MongoFriendship)) return false;

        MongoFriendship that = (MongoFriendship) o;

        if (getObjectId() != null ? !getObjectId().equals(that.getObjectId()) : that.getObjectId() != null)
            return false;
        if (getFriendship() != that.getFriendship()) return false;
        return getProfiles() != null ? getProfiles().equals(that.getProfiles()) : that.getProfiles() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getFriendship() != null ? getFriendship().hashCode() : 0);
        result = 31 * result + (getProfiles() != null ? getProfiles().hashCode() : 0);
        return result;
    }

}
