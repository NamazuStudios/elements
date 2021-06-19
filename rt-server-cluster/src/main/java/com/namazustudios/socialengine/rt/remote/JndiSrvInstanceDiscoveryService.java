package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.AsyncPublisher;
import com.namazustudios.socialengine.rt.ConcurrentLockedPublisher;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static java.util.Collections.emptySortedSet;
import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.TimeUnit.SECONDS;

public class JndiSrvInstanceDiscoveryService implements InstanceDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(JndiSrvInstanceDiscoveryService.class);

    private static final long DNS_LOOKUP_POLLING_RATE = 1;

    private static final TimeUnit DNS_LOOKUP_POLLING_RATE_UNITS = SECONDS;

    public static final String SRV_QUERY = "com.namazustudios.socialengine.rt.srv.query";

    public static final String SRV_SERVERS = "com.namazustudios.socialengine.rt.srv.servers";

    private String srvQuery;

    private String srvServers;

    private final AtomicReference<JndiSrvInstanceDiscoveryService.SrvDiscoveryContext> context = new AtomicReference<>();

    static {
        Lookup.getDefaultCache(DClass.IN).setMaxCache(0);
        Lookup.getDefaultCache(DClass.IN).setMaxNCache(0);
    }

    @Override
    public void start() {

        final JndiSrvInstanceDiscoveryService.SrvDiscoveryContext context = new JndiSrvInstanceDiscoveryService.SrvDiscoveryContext();

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

    private class SrvDiscoveryContext {

        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(JndiSrvInstanceDiscoveryService.class + " refresher.");
            thread.setUncaughtExceptionHandler((t, e) -> logger.error("Caught exception in {}", t, e));
            return thread;
        });

        private DirContext dirContext;

        private Set<JndiInstanceHostInfo> lookupResultSet = new HashSet<>();

        private final Lock lock = new ReentrantLock();

        private final AsyncPublisher<InstanceHostInfo> onDisovery = new ConcurrentLockedPublisher<>(lock, scheduler::submit);

        private final AsyncPublisher<InstanceHostInfo> onUndiscovery = new ConcurrentLockedPublisher<>(lock, scheduler::submit);

        public void start() {

            logger.info("Using SRV FQDN {} querying servers {}", getSrvQuery(), getSrvServers());

            final var env = new Hashtable<>();
            env.put(Context.AUTHORITATIVE, "true");

            final var servers = getSrvServers().trim();
            if (!servers.isEmpty()) env.put(Context.PROVIDER_URL, getSrvServers());

            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

            try {
                dirContext = new InitialDirContext(env);
            } catch (NamingException e) {
                throw new InternalException(e);
            }

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
            } catch (NameNotFoundException ex) {

                logger.info("Query '{}' returned zero no results.", getSrvQuery());

                if (!lookupResultSet.isEmpty()) {
                    logger.info("Removing all known hosts.");
                    lookupResultSet.forEach(onUndiscovery::publishAsync);
                    lookupResultSet = emptySortedSet();
                }

            } catch (Exception ex) {
                logger.error("Error querying SRV records.", ex);
            } finally {
                lock.unlock();
            }
        }

        private SortedSet<JndiInstanceHostInfo> query() throws NamingException {
            final var attributes = dirContext.getAttributes(getSrvQuery(), new String[]{"SRV"});
            return JndiInstanceHostInfo.parse("tcp", attributes.get("srv"));
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

            try {
                dirContext.close();
            } catch (NamingException e) {
                logger.error("Could not stop JDNI context.", e);
            }

        }

        public Collection<InstanceHostInfo> getRemoteConnections() {
            return unmodifiableSet(lookupResultSet);
        }

    }

    public static void main(final String[] args) throws NamingException {

        final var env = new Hashtable<>();
        env.put(Context.AUTHORITATIVE, "true");
        env.put(Context.PROVIDER_URL, "dns://127.0.0.1:5353");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        final var context = new InitialDirContext(env);

        for (int i = 0; i < 100; ++i) {
            final var result = context.getAttributes("_elements._tcp.internal", new String[]{"SRV"});
            final var attributes = result.get("srv");
            final var nfos = JndiInstanceHostInfo.parse("tcp", attributes);
            System.out.println("Host Info: " + nfos);
        }

        System.out.println("Done!");

    }

}
