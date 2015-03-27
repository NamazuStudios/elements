package com.namazustudios.promotion.dao.mongo.model;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity("entrant")
public class MongoSteamEntrant extends MongoBasicEntrant {

    @Property
    private String steamId;

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

}
