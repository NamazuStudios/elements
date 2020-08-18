package com.namazustudios.socialengine.model.application;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the application profile and any associated metadata, such as APNS certificate
 * or other information.
 *
 * Created by patricktwohig on 5/23/17.
 */
public class IosApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    private String applicationId;

    private String appleSignInPrivateKey;

    /**
     * Gets the Application ID, as defined in the AppStore (com.mycompany.app)
     * @return the app id
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID, as deinfed in the AppStore (com.mycompany.app)
     * @param applicationId
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Gets the Apple Sign-In private key.
     *
     * @return the apple-sign in private key.
     */
    public String getAppleSignInPrivateKey() {
        return appleSignInPrivateKey;
    }

    /**
     * Sets the Apple Sign-In Private Key
     *
     * @param appleSignInPrivateKey
     */
    public void setAppleSignInPrivateKey(String appleSignInPrivateKey) {
        this.appleSignInPrivateKey = appleSignInPrivateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IosApplicationConfiguration that = (IosApplicationConfiguration) o;
        return Objects.equals(getApplicationId(), that.getApplicationId()) &&
                Objects.equals(getAppleSignInPrivateKey(), that.getAppleSignInPrivateKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getApplicationId(), getAppleSignInPrivateKey());
    }

}
