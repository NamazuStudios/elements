package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 8/5/15.
 */
public interface Constants {

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

    /**
     * The system property which defines the instance discovery service to use.
     */
    String INSTANCE_DISCOVERY_SERVICE = "com.namazustudios.socialengine.rt.instance.discovery.service";

    /**
     * When using SRV based discovery of instances this defines the actual SRV query to use when discovering instances.
     * This feature is implementation specific and may require different configuration depending on the configuration.
     */
    String SRV_QUERY = "com.namazustudios.socialengine.rt.srv.query";

    /**
     * When using SRV based discovery of instances this defines the actual DNS Servers against which to run queries.
     * This feature is implementation specific and may require different configuration depending on the configuration.
     */
    String SRV_SERVERS = "com.namazustudios.socialengine.rt.srv.servers";

}
