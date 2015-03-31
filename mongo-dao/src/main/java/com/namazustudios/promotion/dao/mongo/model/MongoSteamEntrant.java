package com.namazustudios.promotion.dao.mongo.model;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity(value = "entrant", noClassnameStored = true)
public class MongoSteamEntrant extends MongoBasicEntrant {

    @Property("steam_id")
    private String steamId;

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

}
