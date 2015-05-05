package com.namazustudios.socialengine;

/**
 * Created by patricktwohig on 4/6/15.
 */
public interface Constants {

    /**
     * The number of max results the server will return for any resource.  THis is capped
     * to avoid DDoS attacks.
     */
    public static final String QUERY_MAX_RESULTS = "com.namazustudios.socialengine.query.max.results";

    /**
     * The Digest provider instance for hashing passwords.
     */
    public static final String PASSWORD_DIGEST = "com.namazustudios.socialengine.password.digest";

    /**
     * The algorithm for hashing passwords.
     */
    public static final String PASSWORD_DIGEST_ALGORITHM = "com.namazustudios.socialengine.password.digest.algorithm";

    /**
     * The character encoding used for hashing passwords.
     */
    public static final String PASSWORD_ENCODING = "com.namazustudios.socialengine.password.encoding";

    /**
     * The short-link base for all urls generated.
     */
    public static final String SHORT_LINK_BASE = "com.namazustudios.socialengine.short.link.base";

    /**
     * The system property which defines the configuration file path
     */
    public static final String PROPERTIES_FILE = "com.namazustudios.socialengine.configuration.properties";

    /**
     * The default property file to configure the server.
     */
    public static final String DEFAULT_PROPERTIES_FILE = "socialengine-configuration.properties";

}
