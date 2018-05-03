package com.namazustudios.socialengine;

/**
 * Created by patricktwohig on 4/6/15.
 */
public interface Constants {

    /**
     * Used to controlt he maxmimum number of results returned via the REST api.  This cap is
     * enforced to avoid an undue strain on the server by malicicous users requesting
     * exceptionally large number of search results.
     */
    String QUERY_MAX_RESULTS = "com.namazustudios.socialengine.query.max.results";

    /**
     * The Digest provider instance for hashing passwords.
     */
    String PASSWORD_DIGEST = "com.namazustudios.socialengine.password.digest";

    /**
     * The algorithm for hashing passwords.
     */
    String PASSWORD_DIGEST_ALGORITHM = "com.namazustudios.socialengine.password.digest.algorithm";

    /**
     * The character encoding used for hashing passwords.
     */
    String PASSWORD_ENCODING = "com.namazustudios.socialengine.password.encoding";

    /**
     * The short-link base for all urls generated.
     */
    String SHORT_LINK_BASE = "com.namazustudios.socialengine.short.link.base";

    /**
     * The system property which defines the configuration file path
     */
    String PROPERTIES_FILE = "com.namazustudios.socialengine.configuration.properties";

    /**
     * The default property file to configure the server.
     */
    String DEFAULT_PROPERTIES_FILE = "socialengine-configuration.properties";

    /**
     * The web API prefix.  This is the location from which all API requests are served relative to
     * the context root.  Leaving this unspecified will serve the API out of the context root.
     */
    String API_PREFIX = "com.namazustudios.socialengine.api.prefix";

    /**
     * The web API root.  This is the full outside URL of the API endpoint.
     */
    String API_OUTSIDE_URL = "com.namazustudios.socialengine.api.url";

    /**
     * The web API root.  This is the full outside URL of the API endpoint.
     */
    String DOC_OUTSIDE_URL = "com.namazustudios.socialengine.doc.url";

    /**
     * The allowed CORS origins for the request.
     */
    String CORS_ALLOWED_ORIGINS = "com.namazustudios.socialengine.cors.allowed.origins";

    /**
     * The async request timeout limit.
     */
    String ASYNC_TIMEOUT_LIMIT = "com.namazustudios.socialengine.async.timeout.limit";

    /**
     * The async request timeout limit.
     */
    String CODE_SERVE_URL = "com.namazustudios.socialengine.code.serve.url";

    /**
     * The storage directory for the git repositories shared among instances that need to have
     * access to git repositories.
     */
    String GIT_STORAGE_DIRECTORY = "com.namazustudios.socialengine.git.storage.directory";

    /**
     * The HTTP tunneling root URL.  This is the base URL where the http rt-http service
     * services requests.
     */
    String HTTP_TUNNEL_URL = "com.namazustudios.socialengine.http.tunnel.url";

    /**
     * Used to specify the port that the http tunnel will run against.
     */
    String HTTP_TUNNEL_PORT = "com.namazustudios.socialengine.git.http.tunnel.port";

    /**
     * Used to specify the session timeout, in seconds
     */
    String SESSION_TIMEOUT_SECONDS = "com.namazustudios.socialengine.session.timeout.seconds";

    /**
     * Used to specify the mock session timeout.  If this is
     */
    String MOCK_SESSION_TIMEOUT_SECONDS = "com.namazustudios.socialengine.mock.session.timeout.seconds";

    /**
     * Used to specify the randomly generated password.
     */
    String GENERATED_PASSWORD_LENGTH = "com.namazustudios.socialengine.mock.generated.password.length";

    /**
     * Defines some useful regex patterns.
     */
    interface Regexp {

        /**
         * Non-blank string.
         */
        String NO_WHITE_SPACE = "^\\S+$";

        /**
         * Alpha-numeric only.  Allows underscore, but does not allow the word to start with an underscore.
         */
        String WORD_ONLY = "[^_]\\w+";

        /**
         * A very simple validator
         */
        String EMAIL_ADDRESS = "^(.+)@(.+)$";

    }

}
