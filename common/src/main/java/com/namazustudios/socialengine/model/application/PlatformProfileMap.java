package com.namazustudios.socialengine.model.application;

/**
 * Represents the mapping betweent he {@link Application} object and the various
 * platform-specific profile information.  This is here to provide a simple type-safe
 * mapping.
 *
 * Created by patricktwohig on 7/9/15.
 */
public class PlatformProfileMap {

    private PSNApplicationProfile playstation4Profile;

    private PSNApplicationProfile playstationVitaProfile;

    /**
     * Gets the profile information for the PlayStation(tm) 4 connection.
     */
    public PSNApplicationProfile getPlaystation4Profile() {
        return playstation4Profile;
    }

    /**
     * Sets the PlayStation(tm) 4 profile.
     *
     * @param playstation4Profile
     */
    public void setPlaystation4Profile(PSNApplicationProfile playstation4Profile) {
        this.playstation4Profile = playstation4Profile;
    }

    /**
     * Gets the profile information for the PlayStation(tm) Vita connection.
     */
    public PSNApplicationProfile getPlaystationVitaProfile() {
        return playstationVitaProfile;
    }

    /**
     * Sets the PlayStation(tm) 4 profile.
     *
     * @param playstationVitaProfile
     */
    public void setPlaystationVitaProfile(PSNApplicationProfile playstationVitaProfile) {
        this.playstationVitaProfile = playstationVitaProfile;
    }

}
