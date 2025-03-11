package dev.getelements.elements.rt.remote;

import dev.getelements.elements.sdk.util.AsyncPublisher;
import dev.getelements.elements.sdk.util.ConcurrentLockedPublisher;
import dev.getelements.elements.rt.Constants;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.util.HostList;
import dev.getelements.elements.sdk.util.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class JndiSrvInstanceDiscoveryService implements InstanceDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(JndiSrvInstanceDiscoveryService.class);

    private static final long DNS_LOOKUP_POLLING_RATE = 1;

    private static final TimeUnit DNS_LOOKUP_POLLING_RATE_UNITS = SECONDS;

    public static final String SRV_AUTHORITATIVE = "dev.getelements.elements.rt.srv.authoritative";

    private String srvQuery;

    private String srvServers;

    private boolean authoritative;

    private final Lock lock = new ReentrantLock();

    private volatile JndiSrvInstanceDiscoveryService.SrvDiscoveryContext context;

    @Override
    public void start() {
        try (final var monitor = Monitor.enter(lock)) {
            if (context == null) {
                context = new JndiSrvInstanceDiscoveryService.SrvDiscoveryContext();
                context.start();
            } else {
                throw new IllegalStateException("Already started.");
            }
        }
    }

    @Override
    public void stop() {
        try (final var monitor = Monitor.enter(lock)) {
            if (context == null) {
                throw new IllegalStateException("Not running.");
            } else {
                final var context = this.context;
                this.context = null;
                context.stop();
            }
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
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    public String getSrvQuery() {
        return srvQuery;
    }

    @Inject
    public void setSrvQuery(@Named(Constants.SRV_QUERY) String srvQuery) {
        this.srvQuery = srvQuery;
    }

    public String getSrvServers() {
        return srvServers;
    }

    @Inject
    public void setSrvServers(@Named(Constants.SRV_SERVERS) String srvServers) {
        this.srvServers = srvServers;
    }

    public boolean isAuthoritative() {
        return authoritative;
    }

    @Inject
    public void setAuthoritative(@Named(SRV_AUTHORITATIVE) boolean authoritative) {
        this.authoritative = authoritative;
    }

    private class SrvDiscoveryContext {

        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(JndiSrvInstanceDiscoveryService.class + " refresher.");
            thread.setUncaughtExceptionHandler((t, e) -> logger.error("Caught exception in {}", t, e));
            return thread;
        });

        private List<DirContext> dirContexts;

        private Set<JndiInstanceHostInfo> lookupResultSet = new HashSet<>();

        private final Lock lock = new ReentrantLock();

        private final AsyncPublisher<InstanceHostInfo> onDisovery = new ConcurrentLockedPublisher<>(lock, scheduler::submit);

        private final AsyncPublisher<InstanceHostInfo> onUndiscovery = new ConcurrentLockedPublisher<>(lock, scheduler::submit);

        public void start() {

            logger.info("Using SRV FQDN {} querying servers {}", getSrvQuery(), getSrvServers());

            dirContexts = new HostList()
                .with(getSrvServers())
                .get()
                .map(hosts -> hosts.stream().map(host -> {

                    final var env = new Hashtable<>();
                    env.put("networkaddress.cache.ttl", "-1");
                    env.put(Context.AUTHORITATIVE, Boolean.toString(isAuthoritative()));
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
                    env.put(Context.PROVIDER_URL, host);

                    try {
                        return (DirContext) new InitialDirContext(env);
                    } catch (NamingException ex) {
                        throw new InternalException(ex);
                    }

                })
                .collect(toList()))
                .orElseGet(() -> {

                    final var env = new Hashtable<>();
                    env.put(Context.AUTHORITATIVE, Boolean.toString(isAuthoritative()));
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

                    try {
                        return List.of((DirContext) new InitialDirContext(env));
                    } catch (NamingException ex) {
                        throw new InternalException(ex);
                    }

                });

            scheduler.scheduleAtFixedRate(
                this::refresh,
                0,
                DNS_LOOKUP_POLLING_RATE,
                DNS_LOOKUP_POLLING_RATE_UNITS);

        }

        private void refresh() {
            try {
                lock.lock();
                final var nfos = query();
                update(nfos);
            } finally {
                lock.unlock();
            }
        }

        private SortedSet<JndiInstanceHostInfo> query() {
            return dirContexts
                .stream()
                .flatMap(dirContext -> {

                    Stream<JndiInstanceHostInfo> s = Stream.empty();

                    try {
                        final var attributes = dirContext.getAttributes(getSrvQuery(), new String[]{"SRV"});
                        s = JndiInstanceHostInfo.parse("tcp", attributes.get("srv")).stream();
                    }  catch (NameNotFoundException ex) {
                        logger.info("No hosts found for record {}", getSrvQuery());
                    } catch (Exception ex) {
                        logger.error("Error querying SRV records.", ex);
                    }

                    return s;

                }).collect(toCollection(TreeSet::new));
        }

        private void update(final SortedSet<JndiInstanceHostInfo> update) {

            if (lookupResultSet.equals(update)) {
                logger.debug("No change between {} -> {}. Ignoring.", lookupResultSet, update);
                return;
            }

            final var toAdd = new TreeSet<>(update);
            toAdd.removeAll(lookupResultSet);

            final var toRemove = new TreeSet<>(lookupResultSet);
            toRemove.removeAll(update);

            toAdd.forEach(onDisovery::publishAsync);
            toRemove.forEach(onUndiscovery::publishAsync);

            logger.info("Discovery Update:\n  Update: {} -> {}\n  Added: {}  \nRemoved: {}\n",
                    lookupResultSet, update,
                    toAdd, toRemove);

            lookupResultSet = update;

        }

        public void stop() {

            try {

                scheduler.shutdown();

                if (scheduler.awaitTermination(5, TimeUnit.MINUTES)) {
                    logger.info("Terminated successfully.");
                } else {
                    logger.warn("Termination timed out.");
                }

            } catch (InterruptedException ex) {
                logger.error("Interrupted while shutting down.", ex);
            }

            dirContexts.forEach(dirContext -> {
                try {
                    dirContext.close();
                } catch (NamingException e) {
                    logger.error("Could not stop JDNI context.", e);
                }
            });

        }

        public Collection<InstanceHostInfo> getRemoteConnections() {
            return unmodifiableSet(lookupResultSet);
        }

    }

}
