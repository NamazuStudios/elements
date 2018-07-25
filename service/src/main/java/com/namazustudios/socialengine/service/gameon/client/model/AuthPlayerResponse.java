package com.namazustudios.socialengine.service.gameon.client.model;

public class AuthPlayerResponse {

    private String sessionId;

    private String sessionApiKey;

    private long sessionExpirationDate;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionApiKey() {
        return sessionApiKey;
    }

    public void setSessionApiKey(String sessionApiKey) {
        this.sessionApiKey = sessionApiKey;
    }

    public long getSessionExpirationDate() {
        return sessionExpirationDate;
    }

    public void setSessionExpirationDate(long sessionExpirationDate) {
        this.sessionExpirationDate = sessionExpirationDate;
    }

}
