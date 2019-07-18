//package com.namazustudios.socialengine.rt.srv;
//
//import com.google.common.collect.ImmutableSet;
//import com.google.common.net.HostAndPort;
//import com.spotify.dns.*;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.stream.Collectors;
//
//public class SpotifySrvMonitorService implements SrvMonitorService, ErrorHandler, ChangeNotifier.Listener<LookupResult> {
//
//    private String fqdn;
//    private boolean monitoring = false;
//
//    private AtomicReference<Map<HostAndPort, SrvRecord>> atomicSrvRecords = new AtomicReference<>(new HashMap<>());
//
//    private AtomicReference<Set<SrvMonitorServiceListener>> atomicSrvMonitorServiceListeners = new AtomicReference<>(new HashSet<>());
//
//    private DnsSrvWatcher<LookupResult> watcher;
//    private ChangeNotifier<LookupResult> notifier;
//
//    public boolean start(final String fqdn) {
//        if (monitoring) {
//            throw new IllegalStateException("SpotifySrvMonitorService is already started.");
//        }
//
//        DnsSrvWatcher<LookupResult> watcher = createSpotifyWatcher();
//        this.watcher = watcher;
//
//        try {
//            ChangeNotifier<LookupResult> notifier = this.watcher.watch(fqdn);
//            this.notifier = notifier;
//            this.notifier.setListener(this, true);
//
//            this.fqdn = fqdn;
//
//            monitoring = true;
//
//            // TODO: delete this!
//            new java.util.Timer().schedule(
//                    new java.util.TimerTask() {
//                        @Override
//                        public void run() {
////                            final SrvRecord srvRecord = new SrvRecord("appnode.tcp.namazustudios.com.", 28883, 10, 10, 10);
//                            final SrvRecord srvRecord = new SrvRecord("localhost.", 28883, 10, 10, 10);
//                            notifyCreationListeners(srvRecord);
//                            final SrvRecord srvRecord2 = new SrvRecord("localhost.", 28884, 10, 10, 10);
//                            notifyCreationListeners(srvRecord2);
//                        }
//                    },
//                    5000
//            );
//
//            return true;
//        }
//        catch (DnsException e) {
//            this.watcher = null;
//            this.notifier = null;
//            monitoring = false;
//
//            return false;
//        }
//    }
//
//    private DnsSrvWatcher<LookupResult> createSpotifyWatcher() {
//        DnsSrvResolver resolver = DnsSrvResolvers.newBuilder()
//                .cachingLookups(true)
//                .retainingDataOnFailures(true)  // TODO: we may erroneously assume torn-down instances still exist b/c of this,
//                                                //  not sure how often a failure may occur or what exactly Spotify defines as a failure
//                .dnsLookupTimeoutMillis(1000)   // TODO: load this from params
//                .build();
//
//        DnsSrvWatcher<LookupResult> watcher = DnsSrvWatchers.newBuilder(resolver)
//                .polling(1, TimeUnit.SECONDS)   // TODO: load this from params
//                .withErrorHandler(this)
//                .build();
//
//        return watcher;
//    }
//
//    public void stop() {
//        if (!monitoring) {
//            throw new IllegalStateException("SpotifySrvMonitorService has not been started.");
//        }
//
//        if (notifier != null) {
//            notifier.close();
//        }
//
//        monitoring = false;
//        fqdn = null;
//        watcher = null;
//        notifier = null;
//
//        synchronized(atomicSrvRecords) {
//            final Map<HostAndPort, SrvRecord> srvRecords = atomicSrvRecords.get();
//            srvRecords.clear();
//        }
//
//        synchronized(atomicSrvMonitorServiceListeners) {
//            final Set<SrvMonitorServiceListener> srvMonitorServiceListeners = atomicSrvMonitorServiceListeners.get();
//            srvMonitorServiceListeners.clear();
//        }
//    }
//
//    @Override
//    public void onChange(ChangeNotifier.ChangeNotification<LookupResult> changeNotification) {
//        synchronized (atomicSrvRecords) {
//            final Map<HostAndPort, SrvRecord> srvRecords = atomicSrvRecords.get();
//            final Set<HostAndPort> listedHostsAndPorts = new HashSet<>();
//
//            for (LookupResult lookupResult : changeNotification.current()) {
//                final HostAndPort hostAndPort = HostAndPort.fromParts(lookupResult.host(), lookupResult.port());
//                listedHostsAndPorts.add(hostAndPort);
//
//                if (srvRecords.containsKey(hostAndPort)) {
//                    final SrvRecord srvRecord = srvRecords.get(hostAndPort);
//                    boolean updated = srvRecord.updateFromLookupResultIfNecessary(lookupResult);
//
//                    if (updated) {
//                        notifyUpdateListeners(srvRecord);
//                    }
//                }
//                else {
//                    final SrvRecord srvRecord = SrvRecord.createFromLookupResult(lookupResult);
//                    srvRecords.put(hostAndPort, srvRecord);
//
//                    notifyCreationListeners(srvRecord);
//                }
//            }
//
//            final Set<HostAndPort> delistedHostsAndPorts = new HashSet<>(srvRecords.keySet());
//            delistedHostsAndPorts.removeAll(listedHostsAndPorts);
//
//            for (HostAndPort delistedHostAndPort : delistedHostsAndPorts) {
//                final SrvRecord srvRecord = srvRecords.get(delistedHostAndPort);
//                srvRecords.remove(delistedHostAndPort);
//
//                notifyDeletionListeners(srvRecord);
//            }
//        }
//    }
//
//    private void notifyCreationListeners(SrvRecord srvRecord) {
//        final Set<SrvMonitorServiceListener> srvMonitorServiceListeners = atomicSrvMonitorServiceListeners.get();
//        for (SrvMonitorServiceListener srvMonitorServiceListener : srvMonitorServiceListeners) {
//            srvMonitorServiceListener.onSrvRecordCreated(srvRecord);
//        }
//    }
//
//    private void notifyUpdateListeners(SrvRecord srvRecord) {
//        final Set<SrvMonitorServiceListener> srvMonitorServiceListeners = atomicSrvMonitorServiceListeners.get();
//        for (SrvMonitorServiceListener srvMonitorServiceListener : srvMonitorServiceListeners) {
//            srvMonitorServiceListener.onSrvRecordUpdated(srvRecord);
//        }
//    }
//
//    private void notifyDeletionListeners(SrvRecord srvRecord) {
//        final Set<SrvMonitorServiceListener> srvMonitorServiceListeners = atomicSrvMonitorServiceListeners.get();
//        for (SrvMonitorServiceListener srvMonitorServiceListener : srvMonitorServiceListeners) {
//            srvMonitorServiceListener.onSrvRecordDeleted(srvRecord);
//        }
//    }
//
//    @Override
//    public void registerListener(SrvMonitorServiceListener listener) {
//        final Set<SrvMonitorServiceListener> srvMonitorServiceListeners = atomicSrvMonitorServiceListeners.get();
//        srvMonitorServiceListeners.add(listener);
//    }
//
//    @Override
//    public boolean unregisterListener(SrvMonitorServiceListener listener) {
//        final Set<SrvMonitorServiceListener> srvMonitorServiceListeners = atomicSrvMonitorServiceListeners.get();
//        return srvMonitorServiceListeners.remove(listener);
//    }
//
//    @Override
//    public Set<SrvMonitorServiceListener> getListeners() {
//        final Set<SrvMonitorServiceListener> srvMonitorServiceListeners = atomicSrvMonitorServiceListeners.get();
//        return ImmutableSet.copyOf(srvMonitorServiceListeners);
//    }
//
//    @Override
//    public void handle(String fqdn, DnsException exception) {
//        // TODO: determine strategy for handling these spotify DNS exceptions
//    }
//
//    @Override
//    public Set<SrvRecord> getSrvRecords() {
//        final Map<HostAndPort, SrvRecord> srvRecords = atomicSrvRecords.get();
//        return srvRecords.values().stream().collect(Collectors.toSet());
//    }
//
//    public String getFqdn() {
//        return fqdn;
//    }
//
//    public boolean getMonitoring() {
//        return monitoring;
//    }
//}
