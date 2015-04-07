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
    public static final String DIGEST_PROVIDER = "com.namazustudios.promotion.password.digest.provider";

    /**
     * The algorithm for hashing passwords.
     */
    public static final String DIGEST_ALGORITHM = "com.namazustudios.promotion.password.digest.algorithm";

    /**
     * The character encoding used for hashing passwords.
     */
    public static final String PASSWORD_ENCODING = "com.namazustudios.promotion.password.encoding";

}
