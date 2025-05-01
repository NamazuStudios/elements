package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Represents the platform with which SocialEngine integrates.  Each platform
 * profile is a specific type which houses the necessary information to communicate
 * with the platform's web services API.
 *
 * Created by patricktwohig on 7/10/15.
 */
@Schema
public enum ConfigurationCategory implements Serializable {

    /**
     * Represents a matchmaking profile.
     */
    MATCHMAKING(MatchmakingApplicationConfiguration.class),

    /**
     * Represents an application profile for PlayStation(tm) Network for PS4 enabled titles.
     */
    PSN_PS4(PSNApplicationConfiguration.class),

    /**
     * Represents an application profile for PlayStation(tm) Network for PS4 enabled titles.
     */
    PSN_PS5(PSNApplicationConfiguration.class),

    /**
     * Represents an iOS application configuration for the Apple iTunes AppStore
     */
    IOS_APP_STORE(IosApplicationConfiguration.class),

    /**
     * Represents an Android application configuration for Google Play
     */
    ANDROID_GOOGLE_PLAY(GooglePlayApplicationConfiguration.class),

    /**
     * Represents a Facebook application configuration
     */
    FACEBOOK(FacebookApplicationConfiguration.class),

    /**
     * Represents the Firebase application configuration type.
     */
    FIREBASE(FirebaseApplicationConfiguration.class);

    private final Class<? extends ApplicationConfiguration> modelClass;

    ConfigurationCategory(Class<? extends ApplicationConfiguration> modelClass) {
        this.modelClass = modelClass;
    }

    /**
     * Gets the model {@link Class} which represents the {@link ApplicationConfiguration}.
     *
     * @return the {@link Class} for the model
     */
    public Class<? extends ApplicationConfiguration> getModelClass() {
        return modelClass;
    }

}
