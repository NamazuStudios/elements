package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 8/5/15.
 */
public interface Constants {

    /**
     * It should be obvious what this is.
     */
    @Deprecated
    double NANOSECONDS_PER_SECOND = 1000000000.0;

    /**
     * It should be obvious what this is.
     */
    @Deprecated
    double SECONDS_PER_NANOSECOND = 1.0 / NANOSECONDS_PER_SECOND;

    /**
     * Names a component instance that will use reliable transport, eg TCP
     */
    @Deprecated
    String TRANSPORT_RELIABLE = "com.namazustudios.socialengine.rt.TRANSPORT_RELIABLE";

    /**
     * Names a component instance that will use best effort transport, eg UDP
     */
    @Deprecated
    String TRANSPORT_BEST_EFFORT = "com.namazustudios.socialengine.rt.TRANSPORT_BEST_EFFORT";

    /**
     * Names a component instance that will worker (in memory) tranport.
     */
    @Deprecated
    String TRANSPORT_INTERNAL = "com.namazustudios.socialengine.rt.TRANSPORT_INTERNAL";

    /**
     * The name of the object mapper used by instances of the encoder/decoders.
     */
    @Deprecated
    String BSON_OBJECT_MAPPER = "com.namazustudios.socialengine.rt.BSON_OBJECT_MAPPER";

    /**
     * Constant to name the integer bound tot he max envelope size
     */
    @Deprecated
    String MAX_ENVELOPE_SIZE = "com.namazustudios.socialengine.rt.MAX_ENVELOPE_SIZE";

    /**
     * The MDC Context key for the HTTP request.
     */
    String MDC_HTTP_REQUEST = "HTTP_REQUEST";

    /**
     * The timeout for the {@link HandlerContext} in milliseconds.
     */
    String HTTP_TIMEOUT_MSEC = "com.namazustudios.socialengine.rt.http.timeout.msec";

    /**
     * The number of threads used by the {@link SchedulerContext} for timing/scheduling.
     */
    String SCHEDULER_THREADS = "com.namazustudios.socialengine.rt.scheduler.threads";

}
