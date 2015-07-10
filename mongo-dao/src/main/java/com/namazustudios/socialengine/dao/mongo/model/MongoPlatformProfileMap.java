package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.Reference;

/**
 * Created by patricktwohig on 7/10/15.
 */
public class MongoPlatformProfileMap {

    @Reference
    private MongoPSNApplicationProfile playstation4Profile;

    @Reference
    private MongoPSNApplicationProfile playstationVitaProfile;

    public MongoPSNApplicationProfile getPlaystation4Profile() {
        return playstation4Profile;
    }

    public void setPlaystation4Profile(MongoPSNApplicationProfile playstation4Profile) {
        this.playstation4Profile = playstation4Profile;
    }

    public MongoPSNApplicationProfile getPlaystationVitaProfile() {
        return playstationVitaProfile;
    }

    public void setPlaystationVitaProfile(MongoPSNApplicationProfile playstationVitaProfile) {
        this.playstationVitaProfile = playstationVitaProfile;
    }

}
