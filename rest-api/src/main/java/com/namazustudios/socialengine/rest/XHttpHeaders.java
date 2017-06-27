package com.namazustudios.socialengine.rest;

/**
 * A place to store the non-standard HTTP header (and related) constants..
 *
 * Created by patricktwohig on 6/26/17.
 */
public interface XHttpHeaders {

    /**
     * Used in conjunction with the standard Authorization header.  This is used to
     * trigger an attempt to authorize the user via Facebook OAuth tokens.
     */
    String AUTH_TYPE_FACEBOOK = "Facebook";

}
