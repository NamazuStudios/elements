package com.namazustudios.socialengine.remote.jeromq.srv;

import com.spotify.dns.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SpotifySrvMonitor implements SrvMonitor, ErrorHandler, ChangeNotifier.Listener<LookupResult> {

    private String fqdn;
    private boolean monitoring = false;

    private Map<SrvUniqueIdentifier, SrvRecord> srvRecords = new HashMap<>();

    private Set<Consumer<SrvRecord>> srvCreationListeners = new HashSet<>();
    private Set<Consumer<SrvRecord>> srvUpdateListeners = new HashSet<>();
    private Set<Consumer<SrvRecord>> srvDeletionListeners = new HashSet<>();

    private DnsSrvWatcher<LookupResult> watcher;
    private ChangeNotifier<LookupResult> notifier;

    public boolean start(final String fqdn) {
        if (monitoring) {
            throw new IllegalStateException("SpotifySrvMonitor is already started.");
        }

        DnsSrvWatcher<LookupResult> watcher = createSpotifyWatcher();
        this.watcher = watcher;

        try {
            ChangeNotifier<LookupResult> notifier = this.watcher.watch(fqdn);
            this.notifier = notifier;
            this.notifier.setListener(this, true);

            this.fqdn = fqdn;

            monitoring = true;

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
//                            final SrvRecord srvRecord = new SrvRecord("appnode.tcp.namazustudios.com.", 28883, 10, 10, 10);
                            final SrvRecord srvRecord = new SrvRecord("localhost.", 28883, 10, 10, 10);
                            notifyCreationListeners(srvRecord);
                        }
                    },
                    5000
            );

            return true;
        }
        catch (DnsException e) {
            this.watcher = null;
            this.notifier = null;
            monitoring = false;

            return false;
        }
    }

    private DnsSrvWatcher<LookupResult> createSpotifyWatcher() {
        DnsSrvResolver resolver = DnsSrvResolvers.newBuilder()
                .cachingLookups(true)
                .retainingDataOnFailures(true)  // TODO: we may erroneously assume torn-down instances still exist b/c of this,
                                                // TODO: not sure how often a failure may occur or what exactly Spotify defines as a failure
                .dnsLookupTimeoutMillis(1000)   // TODO: load this from params
                .build();

        DnsSrvWatcher<LookupResult> watcher = DnsSrvWatchers.newBuilder(resolver)
                .polling(1, TimeUnit.SECONDS)   // TODO: load this from params
                .withErrorHandler(this)
                .build();

        return watcher;
    }

    public void stop() {
        if (!monitoring) {
            throw new IllegalStateException("SpotifySrvMonitor has not been started.");
        }

        if (notifier != null) {
            notifier.close();
        }

        monitoring = false;
        fqdn = null;
        watcher = null;
        notifier = null;
        srvRecords.clear();
        srvCreationListeners.clear();
        srvUpdateListeners.clear();
        srvDeletionListeners.clear();
    }

    @Override
    public void onChange(ChangeNotifier.ChangeNotification<LookupResult> changeNotification) {
        final Set<SrvUniqueIdentifier> listedSrvUniqueIdentifiers = new HashSet<>();

        for (LookupResult lookupResult : changeNotification.current()) {
            final SrvUniqueIdentifier srvUniqueIdentifier =
                    new SrvUniqueIdentifier(lookupResult.host(), lookupResult.port());
            listedSrvUniqueIdentifiers.add(srvUniqueIdentifier);

            if (srvRecords.containsKey(srvUniqueIdentifier)) {
                final SrvRecord srvRecord = srvRecords.get(srvUniqueIdentifier);
                boolean updated = srvRecord.updateFromLookupResultIfNecessary(lookupResult);

                if (updated) {
                    notifyUpdateListeners(srvRecord);
                }
            }
            else {
                final SrvRecord srvRecord = SrvRecord.createFromLookupResult(lookupResult);
                srvRecords.put(srvUniqueIdentifier, srvRecord);

                notifyCreationListeners(srvRecord);
            }
        }

        final Set<SrvUniqueIdentifier> delistedSrvUniqueIdentifiers = new HashSet<>(srvRecords.keySet());
        delistedSrvUniqueIdentifiers.removeAll(listedSrvUniqueIdentifiers);

        for (SrvUniqueIdentifier delistedSrvUniqueIdentifier : delistedSrvUniqueIdentifiers) {
            final SrvRecord srvRecord = srvRecords.get(delistedSrvUniqueIdentifier);
            srvRecords.remove(delistedSrvUniqueIdentifier);

            notifyDeletionListeners(srvRecord);
        }

    }

    private void notifyCreationListeners(SrvRecord srvRecord) {
        for (Consumer<SrvRecord> srvCreationListener: srvCreationListeners) {
            srvCreationListener.accept(srvRecord);
        }
    }

    private void notifyUpdateListeners(SrvRecord srvRecord) {
        for (Consumer<SrvRecord> srvUpdateListener: srvUpdateListeners) {
            srvUpdateListener.accept(srvRecord);
        }
    }

    private void notifyDeletionListeners(SrvRecord srvRecord) {
        for (Consumer<SrvRecord> srvDeletionListener: srvDeletionListeners) {
            srvDeletionListener.accept(srvRecord);
        }
    }

    @Override
    public void handle(String fqdn, DnsException exception) {
    }

    public void registerOnCreatedSrvRecordListener(Consumer<SrvRecord> consumer) {
        srvCreationListeners.add(consumer);
    }

    public boolean unregisterOnCreatedSrvRecordListener(Consumer<SrvRecord> consumer) {
        return srvCreationListeners.remove(consumer);
    }

    public void registerOnUpdatedSrvRecordListener(Consumer<SrvRecord> consumer) {
        srvUpdateListeners.add(consumer);
    }

    public boolean unregisterOnUpdatedSrvRecordListener(Consumer<SrvRecord> consumer) {
        return srvUpdateListeners.remove(consumer);
    }

    public void registerOnDeletedSrvRecordListener(Consumer<SrvRecord> consumer) {
        srvDeletionListeners.add(consumer);
    }

    public boolean unregisterOnDeletedSrvRecordListener(Consumer<SrvRecord> consumer) {
        return srvDeletionListeners.remove(consumer);
    }

    @Override
    public Set<SrvRecord> getSrvRecords() {
        return srvRecords.values().stream().collect(Collectors.toSet());
    }

    @Override
    public Set<Consumer<SrvRecord>> getSrvCreationListeners() {
        return srvCreationListeners;
    }

    @Override
    public Set<Consumer<SrvRecord>> getSrvUpdateListeners() {
        return srvUpdateListeners;
    }

    @Override
    public Set<Consumer<SrvRecord>> getSrvDeletionListeners() {
        return srvDeletionListeners;
    }

    public String getFqdn() {
        return fqdn;
    }

    public boolean getMonitoring() {
        return monitoring;
    }
}
