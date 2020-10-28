package com.namazustudios.socialengine.dao.mongo.model.match;

import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Reference;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoMatchSnapshot)) return false;

        MongoMatchSnapshot that = (MongoMatchSnapshot) o;

        if (getPlayer() != null ? !getPlayer().equals(that.getPlayer()) : that.getPlayer() != null) return false;
        if (getOpponent() != null ? !getOpponent().equals(that.getOpponent()) : that.getOpponent() != null)
            return false;
        return getLastUpdatedTimestamp() != null ? getLastUpdatedTimestamp().equals(that.getLastUpdatedTimestamp()) : that.getLastUpdatedTimestamp() == null;
    }

    @Override
    public int hashCode() {
        int result = getPlayer() != null ? getPlayer().hashCode() : 0;
        result = 31 * result + (getOpponent() != null ? getOpponent().hashCode() : 0);
        result = 31 * result + (getLastUpdatedTimestamp() != null ? getLastUpdatedTimestamp().hashCode() : 0);
        return result;
    }

}
