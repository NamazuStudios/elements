package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.AsyncPublisher;
import com.namazustudios.socialengine.rt.ConcurrentLockedPublisher;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.util.HostList;
import com.spotify.dns.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Type;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SpotifySrvInstanceDiscoveryService implements InstanceDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifySrvInstanceDiscoveryService.class);

    private static final long DNS_LOOKUP_TIMEOUT = 1000;

    private static final long DNS_LOOKUP_POLLING_RATE = 1;

    private static final TimeUnit DNS_LOOKUP_POLLING_RATE_UNITS = TimeUnit.SECONDS;

    public static final String SRV_QUERY = "com.namazustudios.socialengine.rt.srv.query";

    public static final String SRV_SERVERS = "com.namazustudios.socialengine.rt.srv.servers";

    private String srvQuery;

    private String srvServers;

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
        return context.onDisovery.subscribe(instanceHostInfoConsumer);
    }

    @Override
    public Subscription subscribeToUndiscovery(final Consumer<InstanceHostInfo> instanceHostInfoConsumer) {
        final SrvDiscoveryContext context = getContext();
        return context.onUndiscovery.subscribe(instanceHostInfoConsumer);
    }

    @Override
    public Collection<InstanceHostInfo> getKnownHosts() {
        final SrvDiscoveryContext context = getContext();
        return context.getRemoteConnections();
    }

    private SrvDiscoveryContext getContext() {
        final SrvDiscoveryContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    public String getSrvQuery() {
        return srvQuery;
    }

    @Inject
    public void setSrvQuery(@Named(SRV_QUERY) String srvQuery) {
        this.srvQuery = srvQuery;
    }

    public String getSrvServers() {
        return srvServers;
    }

    @Inject
    public void setSrvServers(@Named(SRV_SERVERS) String srvServers) {
        this.srvServers = srvServers;
    }

    private class SrvDiscoveryContext implements ChangeNotifier.Listener<LookupResult>, ErrorHandler {

        private DnsSrvResolver dnsSrvResolver;

        private DnsSrvWatcher<LookupResult> dnsSrvWatcher;

        private ChangeNotifier<LookupResult> nodeChangeNotifier;

        private Set<InstanceHostInfo> lookupResultSet = new HashSet<>();

        private final Lock lock = new ReentrantLock();

        private final AsyncPublisher<InstanceHostInfo> onDisovery = new ConcurrentLockedPublisher<>(lock);

        private final AsyncPublisher<InstanceHostInfo> onUndiscovery = new ConcurrentLockedPublisher<>(lock);

        public void start() {

            logger.info("Using FQDN {}", getSrvQuery());

            final var builder = new HostList()
                .with(getSrvServers())
                .get()
                .map(hosts -> {
                    logger.info("Using DNS Hosts {}", hosts);
                    return DnsSrvResolvers.newBuilder().servers(hosts);
                })
                .orElseGet(() -> {
                    logger.info("Using default DNS Server.");
                    return DnsSrvResolvers.newBuilder();
                });

            dnsSrvResolver = builder
                    .cachingLookups(true)
                    .dnsLookupTimeoutMillis(DNS_LOOKUP_TIMEOUT)
                .build();

            dnsSrvWatcher = DnsSrvWatchers.newBuilder(dnsSrvResolver)
                    .polling(DNS_LOOKUP_POLLING_RATE, DNS_LOOKUP_POLLING_RATE_UNITS)
                    .withErrorHandler(this)
                .build();

            nodeChangeNotifier = dnsSrvWatcher.watch(getSrvQuery());
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
            return unmodifiableSet(lookupResultSet);
        }

        @Override
        public void onChange(final ChangeNotifier.ChangeNotification<LookupResult> changeNotification) {
            try {

                lock.lock();

                final Set<InstanceHostInfo> update = changeNotification.current()
                    .stream()
                    .map(LookupResultInstanceHostInfo::new)
                    .collect(toSet());

                final Set<InstanceHostInfo> toAdd = new HashSet<>(update);
                toAdd.removeAll(lookupResultSet);

                final Set<InstanceHostInfo> toRemove = new HashSet<>(lookupResultSet);
                toRemove.removeAll(update);

                // Drives all events asynchronously
                toAdd.forEach(onDisovery::publishAsync);
                toRemove.forEach(onUndiscovery::publishAsync);

                lookupResultSet = update;

            } finally {
                lock.unlock();
            }
        }

        @Override
        public void handle(final String fqdn, final DnsException exception) {
            logger.error("Caught exception polling SRV Records for {}", fqdn, exception);
        }

    }

    private static class LookupResultInstanceHostInfo implements InstanceHostInfo {

        private final LookupResult lookupResult;

        public LookupResultInstanceHostInfo(final LookupResult lookupResult) {
            this.lookupResult = lookupResult;
        }

        @Override
        public String getConnectAddress() {
            var host = lookupResult.host();
            host = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
            return String.format("tcp://%s:%d", host, lookupResult.port());
        }

        @Override
        public boolean equals(Object o) {
            return InstanceHostInfo.equals(this, o);
        }

        @Override
        public int hashCode() {
            return InstanceHostInfo.hashCode(this);
        }

    }

}
