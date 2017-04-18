package com.namazustudios.socialengine.rt.mina;

import com.google.common.base.Stopwatch;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.Server;
import com.namazustudios.socialengine.rt.ServerContainer;
import com.namazustudios.socialengine.rt.edge.EdgeResource;
import com.namazustudios.socialengine.rt.edge.SimpleEdgeServer;
import com.namazustudios.socialengine.rt.internal.InternalResource;
import com.namazustudios.socialengine.rt.internal.SimpleInternalServer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.util.IdentityHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by patricktwohig on 9/12/15.
 */
public class MinaSimpleServerContainer implements ServerContainer {

    public static final Logger LOG = LoggerFactory.getLogger(MinaSimpleServerContainer.class);

    private static final int N_THREADS = 5;

    @Inject
    private Server<EdgeResource> simpleEdgeServer;

    @Inject
    private Server<InternalResource> simpleInternalServer;

    @Inject
    @Named(Constants.TRANSPORT_RELIABLE)
    private IoAcceptor reliableIoAcceptor;

    @Inject
    @Named(Constants.TRANSPORT_BEST_EFFORT)
    private IoAcceptor bestEffortIoAcceptor;

    private final ExecutorService containerExecutorService = Executors.newFixedThreadPool(N_THREADS, r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("Container Thread For: " + r);
        return thread;
    });

    @Override
    public RunningInstance run(final SocketAddress... socketAddresses) {

        final Set<Future<Void>> futures = Collections.synchronizedSet(new IdentityHashSet<Future<Void>>());
        final CompletionService<Void> completionService = new ExecutorCompletionService<>(containerExecutorService);

        futures.add(completionService.submit(() -> {
            reliableIoAcceptor.bind(socketAddresses);
            return null;
        }));

        futures.add(completionService.submit(() -> {
            bestEffortIoAcceptor.bind(socketAddresses);
            return null;
        }));

        return new RunningInstance() {

            @Override
            public void waitForShutdown() throws InterruptedException {

                Future<Void> future;

                while ((future = completionService.take()) != null) {

                    try {
                        future.get();
                    } catch (ExecutionException ex) {
                        LOG.error("Caught exception.", ex);
                        throw new InternalException(ex);
                    }

                    futures.remove(future);

                }

            }

            @Override
            public void waitForShutdown(final long timeout, final TimeUnit timeUnit) throws InterruptedException{
                final Stopwatch stopwatch = Stopwatch.createStarted();

                Future<Void> future;

                long remaining = TimeUnit.NANOSECONDS.convert(timeout, timeUnit);

                while ((future = completionService.poll(remaining, TimeUnit.NANOSECONDS)) != null) {

                    try {
                        future.get();
                    } catch (ExecutionException ex) {
                        LOG.error("Caught exception.", ex);
                        throw new InternalException(ex);
                    }

                    remaining -= Math.max(0, stopwatch.elapsed(TimeUnit.NANOSECONDS));
                }

            }

            @Override
            public void shutdown() {

                try {
                    reliableIoAcceptor.unbind();
                } catch (Exception ex) {
                    LOG.error("Caught exception attempting to unbind reliable IO Acceptor.", ex);
                }

                try {
                    bestEffortIoAcceptor.unbind();
                } catch (Exception ex) {
                    LOG.error("Caught exception attempting to unbind best effort IO Acceptor.", ex);
                }

                try {
                    simpleEdgeServer.shutdown();
                } catch (Exception ex) {
                    LOG.error("Caught exception shutting down the edge server.", ex);
                }

                try {
                    simpleInternalServer.shutdown();
                } catch (Exception ex) {
                    LOG.error("Caught exception shutting down the internal server.", ex);
                }


            }

        };

    }

}
