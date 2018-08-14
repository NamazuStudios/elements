package com.namazustudios.socialengine.model;

/**
 * Used by the Web UI to bootstrap the UI.  LazyValue the UI is loaded, the UI will point
 * at a new REST endpoint to make the necessary calls.
 *
 * Created by patricktwohig on 5/9/17.
 */
public class UiConfig {

    private String apiUrl;

    private String gameOnAdminApiUrl;

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getGameOnAdminApiUrl() {
        return gameOnAdminApiUrl;
    }

    public void setGameOnAdminApiUrl(String gameOnAdminApiUrl) {
        this.gameOnAdminApiUrl = gameOnAdminApiUrl;
    }

}
