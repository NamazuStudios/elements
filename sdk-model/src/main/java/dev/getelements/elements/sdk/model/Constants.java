package dev.getelements.elements.sdk.model;

/**
 * Created by patricktwohig on 4/6/15.
 */
public interface Constants {

    /**
     * The Digest provider instance for hashing passwords.
     */
    String PASSWORD_DIGEST = "dev.getelements.elements.password.digest";

    /**
     * Used to controlt he maxmimum number of results returned via the REST api.  This cap is
     * enforced to avoid an undue strain on the server by malicicous users requesting
     * exceptionally large number of search results.
     */
    String QUERY_MAX_RESULTS = "dev.getelements.elements.query.max.results";

    /**
     * The algorithm for hashing passwords.
     */
    String PASSWORD_DIGEST_ALGORITHM = "dev.getelements.elements.password.digest.algorithm";

    /**
     * The character encoding used for hashing passwords.
     */
    String PASSWORD_ENCODING = "dev.getelements.elements.password.encoding";

    /**
     * The short-link base for all urls generated.
     */
    String SHORT_LINK_BASE = "dev.getelements.elements.short.link.base";

    /**
     * The global secret. If blank, no global secret will be used.
     */
    String GLOBAL_SECRET = "dev.getelements.elements.global.secret";

    /**
     * The ELEMENTS_HOME environment variable.
     */
    String CONFIGURATION_DIRECTORY = "conf";

    /**
     * The system property which defines the configuration file path
     */
    String PROPERTIES_FILE = "dev.getelements.elements.configuration.properties";

    /**
     * The default property file to configure the server.
     */
    String DEFAULT_PROPERTIES_FILE = "elements.properties";

    /**
     * The root APP outside URL
     */
    String APP_OUTSIDE_URL = "dev.getelements.elements.app.url";

    /**
     * The web API root.  This is the full outside URL of the API endpoint.
     */
    String API_OUTSIDE_URL = "dev.getelements.elements.api.url";

    /**
     * The web API root.  This is the full outside URL of the API endpoint.
     */
    String DOC_OUTSIDE_URL = "dev.getelements.elements.doc.url";

    /**
     * Gets the outside CDN url
     */
    String CDN_OUTSIDE_URL = "dev.getelements.elements.cdn.url";

    /**
     * The allowed CORS origins for the request.
     */
    String CORS_ALLOWED_ORIGINS = "dev.getelements.elements.cors.allowed.origins";

    /**
     * The async request timeout limit.
     */
    String ASYNC_TIMEOUT_LIMIT = "dev.getelements.elements.async.timeout.limit";

    /**
     * The async request timeout limit.
     */
    String CODE_SERVE_URL = "dev.getelements.elements.code.serve.url";

    /**
     * The HTTP tunneling root URL.  This is the base URL where the http rt-http service
     * services requests.
     */
    String HTTP_TUNNEL_URL = "dev.getelements.elements.http.tunnel.url";

    /**
     * Used to specify the port that the http service will use when binding.
     */
    String HTTP_PORT = "dev.getelements.elements.http.port";

    /**
     * The http bind address.
     */
    String HTTP_BIND_ADDRESS = "dev.getelements.elements.http.bind.address";

    /**
     * Used to specify the port that the http service will use when binding.
     */
    String HTTP_PATH_PREFIX = "dev.getelements.elements.http.path.prefix";

    /**
     * Used to specify the randomly generated password.
     */
    String GENERATED_PASSWORD_LENGTH = "dev.getelements.elements.mock.generated.password.length";

    /**
     * Defines some useful regex patterns.
     */
    interface Regexp {

        //language=JSRegexp
        /**
         * A string containing no white spaces.
         */
        String NO_WHITE_SPACE = "^\\S+$";

        /**
         * Alphanumeric only. Allows underscore and dash.
         */
        String WORD_ONLY = "\\w+";

        //language=JSRegexp
        /**
         * Alphanumeric only.  Allows underscore, but does not allow the word to start with an underscore.
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

        /**
         * Checks for valid phone.
         */
        String PHONE_NB = "([\\.\\+\\-\\s\\/()]*[0-9][\\.\\+\\-\\s\\/()]*){8,15}";

        /**
         * Checks for valid first name. Rules: only alphanumeric, length 2-20
         */
        String FIRST_NAME = "^[A-Za-z0-9 ]{2,20}";

        /**
         * Checks for valid last name. Rules: only alphanumeric, length 3-30, white spaces available
         */
        String LAST_NAME = "^[A-Za-z0-9 ]{3,30}";

        /**
         * Indicates valid Hex regex.
         */
        String HEX_VALID_REGEX = "[0-9a-fA-F]*";

        /**
         * A regex pattern to validate fully qualified Java class names.
         */
        String JAVA_CLASS_NAME = "^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*$";

    }

}
