package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.sql.Timestamp;

/**
 * Created by patricktwohig on 7/30/17.
 */
public class MongoMatchSnapshot {

    @Indexed
    @Reference
    private MongoProfile player;

    @Reference
    private MongoProfile opponent;

    @Indexed
    @Property
    private Timestamp lastUpdatedTimestamp;

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
