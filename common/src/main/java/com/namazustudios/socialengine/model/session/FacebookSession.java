package com.namazustudios.socialengine.model.session;

/**
 * Represents a {@link Session} started from the usage of a Facebook OAuth token.
 */
public class FacebookSession {

    private Session session;

    private String userAccessToken;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

}
