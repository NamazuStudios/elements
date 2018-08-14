package com.namazustudios.socialengine;


public interface GameOnConstants {

    /**
     * The X-Api-Key header.
     */
    String X_API_KEY = "X-Api-Key";

    /**
     * The Session-Id header.
     */
    String SESSION_ID = "Session-Id";

    /**
     * The version v1 base path.
     */
    String VERSION_V1 = "v1";

    /**
     * The base API for Amazon GameOn Game API.
     */
    String BASE_API = "https://api.amazongameon.com";

    /**
     * The base API for Amazon GameOn Admin API.
     */
    String ADMIN_BASE_API = "https://admin-api.amazongameon.com";

    /**
     * Used by client code to determine the service root for the GameOn API
     */
    String GAMEON_SERVICE_ROOT = "com.namazustudios.socialengine.gameon.service.root";

}
