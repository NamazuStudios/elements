package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import org.mongodb.morphia.annotations.*;

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = MongoFriendshipId.class,
        extractor = MongoFriendIdExtractor.class,
        processors = MongoFriendIdProcessor.class))
@SearchableDocument()
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

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof MongoFriendship)) return false;

        MongoFriendship that = (MongoFriendship) o;

        if (isLesserAccepted() != that.isLesserAccepted()) return false;
        if (isGreaterAccepted() != that.isGreaterAccepted()) return false;
        return getObjectId() != null ? getObjectId().equals(that.getObjectId()) : that.getObjectId() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (isLesserAccepted() ? 1 : 0);
        result = 31 * result + (isGreaterAccepted() ? 1 : 0);
        return result;
    }

}
