package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;

/**
 * Created by patricktwohig on 7/21/17.
 */
@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@SearchableDocument(
        fields = {
                @SearchableField(name = "scheme", path = "/scheme"),
                @SearchableField(name = "lastUpdatedTimestamp", path = "/lastUpdatedTimestamp"),
                @SearchableField(name = "playerId",  path = "/player/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class),
                @SearchableField(name = "opponentId",  path = "/opponent/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class)
        })
@Entity(value = "match", noClassnameStored = true)
public class MongoMatch {

    @Id
    private ObjectId objectId;

    @Indexed
    @Reference
    private MongoProfile player;

    @Indexed
    @Property
    private String scheme;

    @Indexed
    @Reference
    private MongoProfile opponent;

    @Indexed
    @Property
    private Timestamp lastUpdatedTimestamp;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public MongoProfile getPlayer() {
        return player;
    }

    public void setPlayer(MongoProfile player) {
        this.player = player;
    }

    public MongoProfile getOpponent() {
        return opponent;
    }

    public void setOpponent(MongoProfile opponent) {
        this.opponent = opponent;
    }

    public Timestamp getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Timestamp lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

}
