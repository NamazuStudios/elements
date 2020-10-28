package com.namazustudios.socialengine.dao.mongo.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity(value = "entrant", noClassnameStored = true)
public class MongoSteamEntrant extends MongoBasicEntrant {


    @Property
    @Indexed(unique = true)
    private String steamId;

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

}
