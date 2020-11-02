package com.namazustudios.socialengine.dao.mongo.model;

import dev.morphia.annotations.*;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity(value = "entrant")
@Indexes({
        @Index(fields = @Field("steamId"), options = @IndexOptions(name = "indexing_test"))
})
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
