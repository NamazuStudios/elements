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
     * The global secret. If blank, no global secret will be used.
     */
    String GLOBAL_SECRET = "com.namazustudios.socialengine.global.secret";

    /**
     * The ELEMENTS_HOME environment variable.
     */
    String ELEMENTS_HOME = "ELEMENTS_HOME";

    /**
     * The default elements configuration directory.
     */
    String ELEMENTS_HOME_DEFAULT = "/opt/elements";

    /**
     * The ELEMENTS_HOME environment variable.
     */
    String CONFIGURATION_DIRECTORY = "conf";

    /**
     * The system property which defines the configuration file path
     */
    String PROPERTIES_FILE = "com.namazustudios.elements.configuration.properties";

    /**
     * The default property file to configure the server.
     */
    String DEFAULT_PROPERTIES_FILE = "elements.properties";

    /**
     * The system property which defines the configuration file path
     *
     * @deprecated Use the file specified in {@link #PROPERTIES_FILE}
     */
    @Deprecated
    String PROPERTIES_FILE_OLD = "com.namazustudios.socialengine.configuration.properties";

    /**
     * The (deprecated) default property file to configure the server
     *
     * @deprecated Use the file specified in {@link #DEFAULT_PROPERTIES_FILE}
     */
    @Deprecated
    String DEFAULT_PROPERTIES_FILE_OLD = "socialengine-configuration.properties";

    /**
     * The DNS Name for the running instance of the application.
     */
    String DNS_NAME = "com.namazustudios.socialengine.dns.name";

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
     * The outside CDN URL For the app
     */
    String CDN_OUTSIDE_URL = "com.namazustudios.socialengine.cdn.url";

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
     * The HTTP tunneling root URL.  This is the base URL where the http rt-http service
     * services requests.
     */
    String HTTP_TUNNEL_URL = "com.namazustudios.socialengine.http.tunnel.url";

    /**
     * Used to specify the port that the http service will use when binding.
     */
    String HTTP_PORT = "com.namazustudios.socialengine.http.port";

    /**
     * The http bind address.
     */
    String HTTP_BIND_ADDRESS = "com.namazustudios.socialengine.http.bind.address";

    /**
     * Used to specify the port that the http service will use when binding.
     */
    String HTTP_PATH_PREFIX = "com.namazustudios.socialengine.http.path.prefix";

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
     * Used to specify the file path for static content.
     */
    String CDN_FILE_DIRECTORY = "com.namazustudios.socialengine.cdnserve.storage.directory";

    /**
     * Used to specify the endpoint file path for cloning static content.
     */
    String CDN_CLONE_ENDPOINT = "com.namazustudios.socialengine.cdnserve.endpoint.clone";

    /**
     * Used to specify the endpoint for serving static content.
     */
    String CDN_SERVE_ENDPOINT = "com.namazustudios.socialengine.cdnserve.endpoint.serve";

    /**
     * Used to specify the host for neo blockchain.
     */
    String NEO_BLOCKCHAIN_HOST = "com.namazustudios.socialengine.blockchain.neo.host";

    /**
     * Used to specify the port for neo blockchain.
     */
    String NEO_BLOCKCHAIN_PORT = "com.namazustudios.socialengine.blockchain.neo.port";

    /**
     * Used to specify the host for neo blockchain.
     */
    String BSC_BLOCKCHAIN_HOST = "com.namazustudios.socialengine.blockchain.bsc.host";

    /**
     * Used to specify the port for neo blockchain.
     */
    String BSC_BLOCKCHAIN_PORT = "com.namazustudios.socialengine.blockchain.bsc.port";

    /**
     * Defines some useful regex patterns.
     */
    interface Regexp {

        //language=JSRegexp
        /**
         * A string containing no white spaces.
         */
        String NO_WHITE_SPACE = "^\\S+$";

        //language=JSRegexp
        /**
         * Alpha-numeric only.  Allows underscore, but does not allow the word to start with an underscore.
         */
        String WHOLE_WORD_ONLY = "[^_]\\w+";

        //language=JSRegexp
        /**
         * A very simple validator for valid email addresses
         */
        String EMAIL_ADDRESS = "^(.+)@(.+)$";

        //language=JSRegexp
        /**
         * Checks for valid base64.
         */
        String BASE_64 = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";

    }

}
