package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 8/5/15.
 */
public interface Constants {

    /**
     * It should be obvious what this is.
     */
    double NANOSECONDS_PER_SECOND = 1000000000.0;

    /**
     * It should be obvious what this is.
     */
    double SECONDS_PER_NANOSECOND = 1.0 / NANOSECONDS_PER_SECOND;

    /**
     * It should be obvious what this is.
     */
    double MILLISECONDS_PER_SECOND = 1000.0;

    /**
     * The default port for communication.
     */
    int DEFAULT_PORT = 28883;

    /**
     * Names a component instance that will use reliable transport, eg TCP
     */
    String TRANSPORT_RELIABLE = "com.namazustudios.socialengine.rt.TRANSPORT_RELIABLE";

    /**
     * Names a component instance that will use best effort transport, eg UDP
     */
    String TRANSPORT_BEST_EFFORT = "com.namazustudios.socialengine.rt.TRANSPORT_BEST_EFFORT";

    /**
     * Names a component instance that will worker (in memory) tranport.
     */
    String TRANSPORT_INTERNAL = "com.namazustudios.socialengine.rt.TRANSPORT_INTERNAL";

    /**
     * The name of the object mapper used by instances of the encoder/decoders.
     */
    String BSON_OBJECT_MAPPER = "com.namazustudios.socialengine.rt.BSON_OBJECT_MAPPER";

    /**
     * Constant to name the integer bound tot he max envelope size
     */
    String MAX_ENVELOPE_SIZE = "com.namazustudios.socialengine.rt.MAX_ENVELOPE_SIZE";

    String MDC_HTTP_REQUEST = "HTTP_REQUEST";

}
