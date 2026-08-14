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
     * Max-age (in seconds) advertised in Cache-Control for publicly-readable large objects.
     */
    String CDN_PUBLIC_MAX_AGE = "dev.getelements.elements.cdn.public.max.age";

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
         * A valid username.
         *
         * <p>Rules:
         * <ul>
         *   <li>Must not be a valid MongoDB ObjectId (exactly 24 hex digits) — prevents usernames
         *       that collide with database ID lookups at the source</li>
         *   <li>No whitespace characters</li>
         *   <li>No ASCII/Unicode control characters ({@code \p{Cc}}) — blocks null bytes, etc.</li>
         *   <li>No Unicode format characters ({@code \p{Cf}}) — blocks RTL/LTR direction overrides
         *       such as U+202E that can spoof displayed names</li>
         *   <li>Length: 1–50 characters</li>
         * </ul>
         * Allows any printable Unicode character otherwise, including CJK and accented Latin,
         * making this safe for international users.
         */
        String USERNAME = "^(?![0-9a-fA-F]{24}$)[^\\s\\p{Cc}\\p{Cf}]{1,50}$";

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
         * Checks for valid first name.
         *
         * <p>Rules:
         * <ul>
         *   <li>Must start with a Unicode letter ({@code \p{L}}) — blocks leading punctuation and
         *       NoSQL-operator prefixes such as {@code $}</li>
         *   <li>Subsequent characters may be Unicode letters, combining marks ({@code \p{M}}, needed
         *       for pre-composed accents stored as base + combining code-point), Unicode digits,
         *       plain space, apostrophe, hyphen, or period</li>
         *   <li>Length: 1–50 characters</li>
         * </ul>
         * This covers international names including accented Latin (é, ñ, ç), CJK single-character
         * given names, hyphenated names (Mary-Jane), names with apostrophes (O'Brien), and names
         * with dots (St. Pierre).
         */
        String FIRST_NAME = "^\\p{L}[\\p{L}\\p{M}\\p{N} '\\-.]{0,49}$";

        /**
         * Checks for valid last name.
         *
         * <p>Rules: same character set as {@link #FIRST_NAME}; length 1–50 characters.
         * The previous 3-character minimum was too restrictive for many Asian and other
         * non-English surnames (e.g. Li, Wu).
         */
        String LAST_NAME = "^\\p{L}[\\p{L}\\p{M}\\p{N} '\\-.]{0,49}$";

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
