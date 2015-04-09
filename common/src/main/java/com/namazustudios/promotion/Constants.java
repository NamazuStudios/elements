package com.namazustudios.promotion;

/**
 * Created by patricktwohig on 4/6/15.
 */
public interface Constants {

    /**
     * The number of max results the server will return for any resource.  THis is capped
     * to avoid DDoS attacks.
     */
    public static final String QUERY_MAX_RESULTS = "com.namazustudios.promotion.query.max.results";

    /**
     * The Digest provider instance for hashing passwords.
     */
    public static final String PASSWORD_DIGEST = "com.namazustudios.promotion.password.digest";

    /**
     * The algorithm for hashing passwords.
     */
    public static final String PASSWORD_DIGEST_ALGORITHM = "com.namazustudios.promotion.password.digest.algorithm";

    /**
     * The character encoding used for hashing passwords.
     */
    public static final String PASSWORD_ENCODING = "com.namazustudios.promotion.password.encoding";

    /**
     * The short-link base for all urls generated.
     */
    public static final String SHORT_LINK_BASE = "com.namazustudios.promotion.short.link.base";

    /**
     * The system property which defines the configuration file path
     */
    public static final String PROPERTIES_FILE = "com.namazustudios.promotions.configuration.properties";

    /**
     * The default property file to configure the server.
     */
    public static final String DEFAULT_PROPERTIES_FILE = "promotions-configuration.properties";

}
