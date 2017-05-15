package com.namazustudios.socialengine.model.application;

/**
 * Represents the platform with which SocialEngine integrates.  Each platform
 * profile is a specific type which houses the necessary information to communicate
 * with the platform's web services API.
 *
 * Created by patricktwohig on 7/10/15.
 */
public enum Platform {

    /**
     * Represents the PlayStation(tm) Network for PS4 enabled titles..
     */
    PSN_PS4(PSNApplicationProfile.class),

    /**
     * Represents the PlayStation(tm) Network for Vita enabled titles..
     */
    PSN_VITA(PSNApplicationProfile.class);

    Platform(final Class<?> profileModelType) {
        this.profileModelType = profileModelType;
    }

    private final Class<?> profileModelType;

    /**
     * Gets the {@link Class} assocaited with the supplied profile model.
     * @return
     */
    public Class<?> getProfileModelType() {
        return profileModelType;
    }

}
