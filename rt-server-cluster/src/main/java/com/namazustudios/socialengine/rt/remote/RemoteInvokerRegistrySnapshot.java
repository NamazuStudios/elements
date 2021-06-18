package com.namazustudios.socialengine.rt.remote;

import com.google.common.collect.Comparators;
import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

class RemoteInvokerRegistrySnapshot {

    private Storage storage = new Storage();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public RemoteInvoker getRemoteInvoker(final NodeId nodeId) {

        final Lock lock = readWriteLock.readLock();

        try {
            lock.lock();
            final RemoteInvoker remoteInvoker = storage.invokersByNode.get(nodeId);
            if (remoteInvoker == null)
                throw new NodeNotFoundException(nodeId);
            return remoteInvoker;
        } finally {
            lock.unlock();
        }

    }

    public RemoteInvoker getBestInvokerForApplication(final ApplicationId applicationId) {

        final Lock lock = readWriteLock.readLock();

        try {
            lock.lock();
            final List<RemoteInvoker> byLoad = storage.invokersByApplication.get(applicationId);
            if (byLoad == null || byLoad.isEmpty()) throw new NodeNotFoundException("Unknown Application: " + applicationId);
            return byLoad.get(0);
        } finally {
            lock.unlock();
        }

    }

    public List<RemoteInvoker> getAllRemoteInvokersForApplication(final ApplicationId applicationId) {

        final Lock lock = readWriteLock.readLock();

        try {
            lock.lock();
            final List<RemoteInvoker> byLoad = storage.invokersByApplication.get(applicationId);
            if (byLoad == null || byLoad.isEmpty()) throw new NodeNotFoundException("Unknown Application: " + applicationId);
            return Collections.unmodifiableList(byLoad);
        } finally {
            lock.unlock();
        }

    }

    public Map<NodeId, RemoteInvoker> getInvokersByNode() {

        final var lock = readWriteLock.readLock();

        try {
            lock.lock();
            return new TreeMap<>(storage.invokersByNode);
        } finally {
            lock.unlock();
        }

    }

    public void clear() {

        final Storage old;
        final Lock lock = readWriteLock.writeLock();

        try {
            lock.lock();
            old = storage;
            storage = new Storage();
        } finally {
            lock.unlock();
        }

        old.clear();

    }

    public RefreshBuilder refresh() {
        return new RefreshBuilder() {

            private Consumer<Storage> prune = s -> {};
            private Set<NodeId> toRetain = new HashSet<>();

            private final List<Consumer<Storage>> updates = new ArrayList<>();

            @Override
            public void commit(final BiConsumer<RemoteInvoker, Exception> cleanup) {


                final Runnable purge;
                final var lock = readWriteLock.writeLock();

                try {
                    lock.lock();

                    // Copy the data in case any of the subsequent operations fail we won't leave things in a state
                    // of undefined behavior.

                    final Storage update = storage.begin();
                    update.cleanup = cleanup;

                    // Apply all updates and prune if necessary.
                    updates.forEach(up -> up.accept(update));
                    prune.accept(update);

                    // Re-sort everything for load balancing consistency as new RemoteInvokers were added and updated
                    // based on load characteristics.
                    update.sort();

                    // Generate the list of objects to purge
                    purge = update.purge();

                    // Finally make the new storage live
                    storage = update;

                } finally {
                    lock.unlock();
                }

                // Purging old connections is deferred as the last step.  At this point everything that needs purged
                // will be removed from the pool and we want to defer the actual destruction of those to avoid having
                // to hold the lock while a shutdown of each remote invoker takes place.

                purge.run();

            }

            @Override
            public RefreshBuilder add(final NodeId nodeId, final double load,
                                      final Supplier<RemoteInvoker> remoteInvokerSupplier) {
                toRetain.add(nodeId);
                updates.add(storage -> storage.add(nodeId, load, remoteInvokerSupplier));
                return this;
            }

            @Override
            public RefreshBuilder remove(final NodeId nodeId) {
                updates.add(storage -> storage.remove(nodeId));
                return this;
            }

            @Override
            public RefreshBuilder remove(final InstanceId instanceId) {
                updates.add(storage -> storage.remove(instanceId));
                return this;
            }

            @Override
            public RefreshBuilder prune() {
                prune = storage -> storage.prune(toRetain);
                return this;
            }

        };
    }

    private static class Storage {

        private BiConsumer<RemoteInvoker, Exception> cleanup = (r, e) -> {
            throw new IllegalStateException("No cleanup routine specified.");
        };

        private final Set<RemoteInvoker> invokersToPurge = new HashSet<>();

        private final Map<NodeId, RemoteInvoker> invokersByNode = new HashMap<>();

        private final Map<ApplicationId, List<RemoteInvoker>> invokersByApplication = new HashMap<>();

        private void add(final NodeId nodeId, final double load,
                         final Supplier<RemoteInvoker> remoteInvokerSupplier) {

            RemoteInvoker invoker = invokersByNode.get(nodeId);

            if (invoker == null) {
                invoker = remoteInvokerSupplier.get();
                invokersByNode.put(nodeId, invoker);
            }

            final ApplicationId applicationId = nodeId.getApplicationId();

            final List<RemoteInvoker> remoteInvokerList = invokersByApplication
                .computeIfAbsent(applicationId, nid -> new ArrayList<>());

            final PriorityRemoteInvoker update = new PriorityRemoteInvoker(invoker, load);

            remoteInvokerList.removeIf(ri -> {
                final PriorityRemoteInvoker pri = (PriorityRemoteInvoker)ri;
                return pri.getDelegate() == update.getDelegate();
            });

            remoteInvokerList.add(update);

        }

        private void remove(final NodeId nodeId) {

            final RemoteInvoker removed = invokersByNode.remove(nodeId);

            if (removed != null) {
                final List<RemoteInvoker> remoteInvokers = invokersByApplication.get(nodeId.getApplicationId());
                remoteInvokers.removeIf(ri -> ((PriorityRemoteInvoker)ri).getDelegate() == removed);
            }

            invokersToPurge.add(removed);

        }

        private void remove(final InstanceId instanceId) {
            invokersByNode.keySet()
                .stream()
                .filter(nid -> instanceId.equals(nid.getInstanceId()))
                .collect(toList())
                .forEach(this::remove);
        }

        private void prune(final Set<NodeId> toRetain) {
            final Set<NodeId> toPurge = new HashSet<>(invokersByNode.keySet());
            toPurge.removeAll(toRetain);
            toPurge.forEach(this::remove);
        }

        private Runnable purge() {

            final List<RemoteInvoker> toPurge = new ArrayList<>(invokersToPurge);
            final BiConsumer<RemoteInvoker, Exception> cleanup = this.cleanup;

            return () -> toPurge.forEach(ri -> {
                try {
                    cleanup.accept(ri, null);
                } catch (Exception ex) {
                    cleanup.accept(ri, ex);
                }
            });

        }

        private void sort() {

            final Comparator<RemoteInvoker> cmp = (o1, o2) -> {
                final var po1 = (PriorityRemoteInvoker) o1;
                final var po2 = (PriorityRemoteInvoker) o2;
                return po1.compareTo(po2);
            };

            invokersByApplication.forEach((id, invokers) -> invokers.sort(cmp.reversed()));

        }

        private void clear() {
            invokersByNode.forEach((nid, ri) -> {
                try {
                    cleanup.accept(ri, null);
                } catch (Exception ex) {
                    cleanup.accept(ri, ex);
                }
            });
        }

        private Storage begin() {
            final Storage copy = new Storage();
            copy.invokersByNode.putAll(invokersByNode);
            invokersByApplication.forEach((k,v) -> copy.invokersByApplication.put(k, new ArrayList<>(v)));
            return copy;
        }

    }

    /**
     * A builder-type that is used to refresh the internal state of a {@link RemoteInvokerRegistrySnapshot}
     */
    public interface RefreshBuilder {

        /**
         * Commits the changes.
         *
         * @param cleanup
         * @return this instance
         */
        void commit(BiConsumer<RemoteInvoker, Exception> cleanup);

        /**
         * Adds a {@link RemoteInvoker} to the operation.  The actual creation of the {@link RemoteInvoker} is deferred
         * until invoking the {@link Supplier<RemoteInvoker>}.
         *
         * @param nodeId the {@link NodeId} for the {@link RemoteInvoker}
         * @param load the load factor representing the quality of the remote node
         * @param remoteInvokerSupplier the {@link Supplier<RemoteInvoker>}
         * @return this instance
         */
        RefreshBuilder add(NodeId nodeId, double load, Supplier<RemoteInvoker> remoteInvokerSupplier);

        /**
         * Removes a {@link RemoteInvoker} to the operation.  The actual creation of the {@link RemoteInvoker} is
         * deferred until invoking the {@link Supplier<RemoteInvoker>}.
         *
         * @param nodeId the {@link NodeId} for the {@link RemoteInvoker}
         * @return this instance
         */
        RefreshBuilder remove(NodeId nodeId);

        /**
         * Removes all {@link RemoteInvoker} instances associated with the {@link InstanceId}.
         *
         * @param instanceId the {@link InstanceId}
         * @return this instance
         */
        RefreshBuilder remove(InstanceId instanceId);

        /**
         * Ensures that after update, any {@link RemoteInvoker} instances not explicitly added are removed from the
         * snapshot.
         * @return this instance
         */
        RefreshBuilder prune();

    }

}
