package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

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
                @SearchableField(name = "gameId",    path = "/gameId"),
                @SearchableField(name = "lastUpdatedTimestamp", path = "/lastUpdatedTimestamp"),
                @SearchableField(name = "playerId",  path = "/player/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class),
                @SearchableField(name = "opponentId",  path = "/player/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class)
        })
@Entity(value = "match", noClassnameStored = true)
public class MongoMatch {

    @Id
    private ObjectId objectId;

    @Indexed
    @Property
    private MongoProfile player;

    @Indexed
    @Property
    private MongoProfile opponent;

    private String gameId;

    private long lastUpdatedTimestamp;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
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

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

}
