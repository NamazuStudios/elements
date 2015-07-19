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
     * Defines some useful regex patterns.
     */
    interface Regexp {

        /**
         * Non-blank string.
         */
        String NON_BLANK_STRING = "\\S+";

        /**
         * Alpha-numeric only
         */
        String WORD_ONLY = "\\w+";

        /**
         * A very simple validator
         */
        String EMAIL_ADDRESS = "^(.+)@(.+)$";

    }

}
