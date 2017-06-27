package com.namazustudios.socialengine.rest;

/**
 * Created by patricktwohig on 6/26/17.
 */
public interface HTTPHeaders {

    /**
     * Represents the application.  The value must be either the application name or
     * identifier.
     */
    String X_APPLICATION = "X-NamazuSocialengineApplication";

    /**
     * Used in conjunction with the specific Auth type header, this will ensure
     * that the
     */
    String AUTH_TYPE_FACEBOOK = "Facebook";

}
