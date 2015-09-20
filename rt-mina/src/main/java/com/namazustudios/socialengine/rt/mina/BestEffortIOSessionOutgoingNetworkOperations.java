package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.*;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;

/**
 * Unlike the {@link ReliableIOSessionOutgoingNetworkOperations}, this provides a mechanism by which
 * {@link Request} instances will produce a {@link Response} code with {@link ResponseCode#TIMEOUT_RETRY}
 * to indicate that the request timed out.
 *
 * Created by patricktwohig on 9/20/15.
 */
public class BestEffortIOSessionOutgoingNetworkOperations implements OutgoingNetworkOperations {

    private static final Logger LOG = LoggerFactory.getLogger(BestEffortIOSessionOutgoingNetworkOperations.class);

    public static final String TIMEOUT = "com.namazustudios.socialengine.rt.mina.BestEffortIOSessionOutgoingNetworkOperations.TIMEOUT";

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable runnable) {
            final Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName(BestEffortIOSessionOutgoingNetworkOperations.class + " timeout thread.");
            return thread;
        }
    });

    @Inject
    @Named(TIMEOUT)
    private double timeout;

    @Inject
    private IoSession ioSession;

    @Inject
    private IncomingNetworkOperations incomingNetworkOperations;

    @Override
    public void dispatch(final Request request) {

        ioSession.write(request);

        executorService.schedule(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                final Response response = SimpleResponse.builder()
                        .from(request)
                        .payload(request.getPayload())
                        .code(ResponseCode.TIMEOUT_RETRY)
                    .build();

                LOG.debug("Timing out request {}", request);
                incomingNetworkOperations.receive(response);

                return null;

            }

        }, Math.round(timeout * Constants.SECONDS_PER_NANOSECOND), TimeUnit.NANOSECONDS);

    }

}
