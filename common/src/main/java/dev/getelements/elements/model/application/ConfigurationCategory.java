package dev.getelements.elements.model.application;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * Represents the platform with which SocialEngine integrates.  Each platform
 * profile is a specific type which houses the necessary information to communicate
 * with the platform's web services API.
 *
 * Created by patricktwohig on 7/10/15.
 */
@ApiModel
public enum ConfigurationCategory implements Serializable {

    /**
     * Represents a matchmaking profile.
     */
    MATCHMAKING,

    /**
     * Represents an application profile for PlayStation(tm) Network for PS4 enabled titles.
     */
    PSN_PS4,

    /**
     * Represents an application profile for PlayStation(tm) Network for Vita enabled titles.
     */
    PSN_VITA,

    /**
     * Represents an iOS application configuration for the Apple iTunes AppStore
     */
    IOS_APP_STORE,

    /**
     * Represents an Android application configuration for Google Play
     */
    ANDROID_GOOGLE_PLAY,

    /**
     * Represents a Facebook application configuration
     */
    FACEBOOK,

    /**
     * Represents the Firebase application configuration type.
     */
    FIREBASE,

    /**
     * Represents an application configuration for Google Sign in
     */
    GOOGLE_SIGN_IN

}
