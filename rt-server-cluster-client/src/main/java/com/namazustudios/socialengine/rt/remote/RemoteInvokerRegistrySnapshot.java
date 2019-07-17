package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class RemoteInvokerRegistrySnapshot {

    private Storage storage = new Storage();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public RemoteInvoker getRemoteInvoker(final NodeId nodeId) {

        final Lock lock = readWriteLock.readLock();

        try {
            lock.lock();
            final RemoteInvoker remoteInvoker = storage.invokersByNode.get(nodeId);
            if (remoteInvoker == null) throw new NodeNotFoundException(nodeId);
            return remoteInvoker;
        } finally {
            lock.unlock();
        }

    }

    public RemoteInvoker getBestInvokerForApplication(final UUID applicationId) {

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

    public List<RemoteInvoker> getAllRemoteInvokersForApplication(final UUID applicationId) {

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

    public void clear() {
        final Lock lock = readWriteLock.writeLock();

        try {
            storage = new Storage();
        } finally {
            lock.unlock();
        }

    }

    public RefreshBuilder refresh() {
        return new RefreshBuilder() {

            private BiConsumer<Storage, Storage> prune = (o, n) -> {};

            private final List<Consumer<Storage>> updates = new ArrayList<>();

            @Override
            public void commit(final BiConsumer<RemoteInvoker, Exception> cleanup) {

                final Lock lock = readWriteLock.writeLock();

                try {

                    lock.lock();

                    final Storage update = storage.copy();
                    update.cleanup = cleanup;
                    updates.forEach(op -> op.accept(storage));
                    prune.accept(storage, update);
                    storage.sort();

                } finally {
                    lock.unlock();
                    storage.purge();
                }

            }

            @Override
            public RefreshBuilder add(final NodeId nodeId, final double load,
                                      final Supplier<RemoteInvoker> remoteInvokerSupplier) {
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
                prune = (existing, update) -> update.prune(existing);
                return this;
            }

        };
    }

    private static class Storage {

        private BiConsumer<RemoteInvoker, Exception> cleanup = (r, e) -> {
            throw new IllegalStateException("No cleanup routine specified.");
        };

        private final List<RemoteInvoker> invokersToPurge = new ArrayList<>();

        private final Map<NodeId, RemoteInvoker> invokersByNode = new HashMap<>();

        private final Map<UUID, List<RemoteInvoker>> invokersByApplication = new HashMap<>();

        private void add(final NodeId nodeId, final double load,
                         final Supplier<RemoteInvoker> remoteInvokerSupplier) {

            RemoteInvoker invoker = invokersByNode.get(nodeId);

            if (invoker == null) {
                invoker = remoteInvokerSupplier.get();
                invokersByNode.put(nodeId, invoker);
            }

            final UUID applicationUuid = nodeId.getApplicationUuid();

            final List<RemoteInvoker> remoteInvokerList = invokersByApplication
                .computeIfAbsent(applicationUuid, nid -> new ArrayList<>());

            remoteInvokerList.add(new PriorityRemoteInvoker(invoker, load));

        }

        private void remove(final NodeId nodeId) {

            final RemoteInvoker removed = invokersByNode.remove(nodeId);

            if (removed != null) {
                final List<RemoteInvoker> remoteInvokers = invokersByApplication.get(nodeId.getApplicationUuid());
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

        private void prune(final Storage existing) {

        }

        private void purge() {
            invokersToPurge.forEach(ri -> {
                try {
                    cleanup.accept(ri, null);
                } catch (Exception ex) {
                    cleanup.accept(ri, ex);
                }
            });
        }

        private void sort() {
            invokersByApplication.forEach((id, invokers) -> Collections.sort(invokers, (o1, o2) -> {
                final PriorityRemoteInvoker po1 = (PriorityRemoteInvoker)o1;
                final PriorityRemoteInvoker po2 = (PriorityRemoteInvoker)o2;
                return po1.compareTo(po2);
            }));
        }

        private Storage copy() {
            final Storage copy = new Storage();
            copy.invokersByNode.putAll(invokersByNode);
            invokersByApplication.forEach((a, i) -> copy.invokersByApplication.put(a, new ArrayList<>(i)));
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
