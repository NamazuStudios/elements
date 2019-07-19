package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.PubSub;
import com.spotify.dns.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.String.format;

public class SpotifySrvInstanceDiscoveryService implements InstanceDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifySrvInstanceDiscoveryService.class);

    private static final long DNS_LOOKUP_TIMEOUT = 1000;

    private static final long DNS_LOOKUP_POLLING_RATE = 1;

    private static final TimeUnit DNS_LOOKUP_POLLING_RATE_UNITS = TimeUnit.SECONDS;

    public static final String CONTROL_SERVICE_NAME = "com.namazustudios.socialengine.rt.srv.control.service";
    public static final String INVOKER_SERVICE_NAME = "com.namazustudios.socialengine.rt.srv.invoker.service";

    private String controlServiceName;

    private String invokerServiceName;

    private final AtomicReference<SrvDiscoveryContext> context = new AtomicReference<>();

    @Override
    public void start() {

        final SrvDiscoveryContext context = new SrvDiscoveryContext();

        if (this.context.compareAndSet(null, context)) {
            context.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final SrvDiscoveryContext context = this.context.getAndSet(null);

        if (context == null) {
            throw new IllegalStateException("Not running.");
        } else {
            context.stop();
        }

    }

    @Override
    public Subscription subscribeToDiscovery(final Consumer<InstanceHostInfo> instanceHostInfoConsumer) {
        final SrvDiscoveryContext context = getContext();
        return context.discovery.subscribe(instanceHostInfoConsumer);
    }

    @Override
    public Subscription subscribeToUndiscovery(final Consumer<InstanceHostInfo> instanceHostInfoConsumer) {
        final SrvDiscoveryContext context = getContext();
        return context.undiscovery.subscribe(instanceHostInfoConsumer);
    }

    @Override
    public Collection<InstanceHostInfo> getRemoteConnections() {
        final SrvDiscoveryContext context = getContext();
        return context.getRemoteConnections();
    }

    private SrvDiscoveryContext getContext() {
        final SrvDiscoveryContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    public String getControlServiceName() {
        return controlServiceName;
    }

    @Inject
    public void setControlServiceName(@Named(CONTROL_SERVICE_NAME) String controlServiceName) {
        this.controlServiceName = controlServiceName;
    }

    public String getInvokerServiceName() {
        return invokerServiceName;
    }

    @Inject
    public void setInvokerServiceName(@Named(INVOKER_SERVICE_NAME) String invokerServiceName) {
        this.invokerServiceName = invokerServiceName;
    }

    private class SrvDiscoveryContext implements ChangeNotifier.Listener<LookupResult>, ErrorHandler {

        private DnsSrvResolver dnsSrvResolver;

        private DnsSrvWatcher<LookupResult> dnsSrvWatcher;

        private ChangeNotifier<LookupResult> nodeChangeNotifier;

        private final PubSub<InstanceHostInfo> discovery = new PubSub<>();

        private final PubSub<InstanceHostInfo> undiscovery = new PubSub<>();

        public void start() {

            dnsSrvResolver = DnsSrvResolvers.newBuilder()
                .cachingLookups(true)
                .dnsLookupTimeoutMillis(DNS_LOOKUP_TIMEOUT)
                .build();

            dnsSrvWatcher = DnsSrvWatchers.newBuilder(dnsSrvResolver)
                .polling(DNS_LOOKUP_POLLING_RATE, DNS_LOOKUP_POLLING_RATE_UNITS)
                .withErrorHandler(this)
                .build();

            nodeChangeNotifier = dnsSrvWatcher.watch(getInvokerServiceName());
            nodeChangeNotifier.setListener(this, true);

        }

        public void stop() {

            try {
                nodeChangeNotifier.close();
            } catch (Exception ex) {
                logger.error("Caught exception closing Change Notifier.", ex);
            }

            try {
                dnsSrvWatcher.close();
            } catch (IOException ex) {
                logger.error("Caught exception closing SRV Watcher.", ex);
            }

        }

        public Collection<InstanceHostInfo> getRemoteConnections() {
            return Collections.emptyList();
        }

        @Override
        public void onChange(final ChangeNotifier.ChangeNotification<LookupResult> changeNotification) {

        }

        @Override
        public void handle(final String fqdn, final DnsException exception) {
            logger.error("Caught exception polling SRV Records for {}", fqdn, exception);
        }

    }

    private static class LookupRecordInstanceHostInfo implements InstanceHostInfo {

        private final LookupResult lookupResult;

        public LookupRecordInstanceHostInfo(LookupResult lookupResult) {
            this.lookupResult = lookupResult;
        }

        @Override
        public String getConnectAddress() {
            return format("tcp://%s:%d", lookupResult.host(), lookupResult.port());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LookupRecordInstanceHostInfo)) return false;
            LookupRecordInstanceHostInfo that = (LookupRecordInstanceHostInfo) o;
            return lookupResult.equals(that.lookupResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lookupResult);
        }

    }

}
