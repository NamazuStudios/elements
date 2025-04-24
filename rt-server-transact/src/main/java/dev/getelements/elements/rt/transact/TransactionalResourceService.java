package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceLoader;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.ReadOnlyTransaction.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.util.FinallyAction.begin;
import static java.util.stream.Collectors.toList;

public class TransactionalResourceService implements ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalResourceService.class);

    private NodeId nodeId;

    private ResourceLoader resourceLoader;

    private TransactionalResourceServicePersistence persistence;

    private final AtomicReference<Context> context = new AtomicReference<>();

    @Override
    public void start() {

        final var context = new Context();

        if (this.context.compareAndSet(null, context)) {
            logger.info("Started.");
        } else {
            throw new IllegalStateException("Already running.");
        }

    }

    @Override
    public void stop() {
        final var context = this.context.getAndSet(null);
        if (context == null) throw new IllegalStateException("Not currently running.");
    }

    @Override
    public boolean exists(final ResourceId resourceId) {
        try (var txn = buildRO().with(resourceId).begin()) {
            return txn.exists(resourceId);
        }
    }

    @Override
    public ResourceAcquisition acquire(final Path path) {

        final var context = getContext();
        final var normalized = normalize(path);

        try (var txn = buildRO().with(normalized).begin()) {

            final var resourceId = txn.getResourceId(normalized);

            final var resource = getContext()
                    .getResident()
                    .computeIfAbsent(resourceId, r -> load(txn, resourceId));

            return new SimpleResourceAcquisition(context, resource.acquire());

        }

    }

    @Override
    public ResourceAcquisition acquire(final ResourceId resourceId) {

        final var context = getContext();

        try (var txn = buildRO().with(resourceId).begin()) {

            final var resource = getContext()
                    .getResident()
                    .computeIfAbsent(resourceId, r -> load(txn, resourceId));

            return new SimpleResourceAcquisition(context, resource.acquire());

        }
    }

    @Override
    public ResourceTransaction acquireWithTransaction(final Path path) {
        final var txn = buildRW().with(path).begin();
        final var resourceId = txn.getResourceId(path);
        return new SimpleResourceTransaction(txn, resourceId);
    }

    @Override
    public ResourceTransaction acquireWithTransaction(final ResourceId resourceId) {
        final var txn = buildRW().with(resourceId).begin();
        return new SimpleResourceTransaction(txn, resourceId);
    }

    private TransactionalResource load(final ReadOnlyTransaction txn, final ResourceId resourceId) {
        try (final var rbc = txn.loadResourceContents(resourceId)) {
             final var loaded = getResourceLoader().load(rbc, false);
            return new TransactionalResource(loaded);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public ResourceAcquisition addAndAcquireResource(final Path path, final Resource resource) {

        final var context = getContext();
        final var resourceId = resource.getId();
        final var destination = normalize(path).appendUUIDIfWildcard();

        try (final var txn = buildRW().with(destination).with(resourceId).begin()) {

            if (txn.exists(resourceId)) {
                throw new InternalException("Resource already present in datastore.");
            }

            txn.linkNewResource(resourceId, destination);
            txn.commit();

            final var transactionalResource = new TransactionalResource(resource);

            final var existing = context
                    .getResident()
                    .putIfAbsent(resourceId, transactionalResource);

            if (existing != null) {
                resource.unload();
                throw new InternalException("Resource already present in resident cache.");
            }

            return new SimpleResourceAcquisition(context, transactionalResource.acquire());

        }

    }

    @Override
    public void addAndReleaseResource(final Path path, final Resource resource) {

        final var resourceId = resource.getId();
        final var destination = normalize(path).appendUUIDIfWildcard();

        try (var txn = buildRW().with(destination).with(resourceId).begin();
             var act = begin(logger).then(resource::unload)) {

            try (var wbc = txn.saveNewResource(destination, resourceId)) {
                resource.serialize(wbc);
            } catch (IOException ex) {
                throw new InternalException(ex);
            }

            txn.commit();

        }

    }

    private static void doRelease(
            final Context context,
            final ReadWriteTransaction txn,
            final TransactionalResource transactionalResource) {

        final var resourceId = transactionalResource.getId();
        transactionalResource.release();

        if (transactionalResource.isFullyReleased()) {

            final var delegate = transactionalResource.getDelegate();

            try (var wbc = txn.updateResource(resourceId);
                 var act = begin(logger)
                         .then(delegate::unload)
                         .then(() -> context.getResident().remove(resourceId))) {
                delegate.serialize(wbc);
            } catch (IOException ex) {
                throw new InternalException(ex);
            }

        }
    }

    @Override
    public Stream<Listing> listStream(final Path path) {

        final var normalized = normalize(path);

        try (var txn = buildRO().with(normalized).begin()) {
            final var listings = txn.list(normalized).collect(toList());
            return listings.stream();
        }

    }

    @Override
    public void link(final ResourceId sourceResourceId, final Path destination) {

        final var normalized = normalize(destination);

        try (var txn = buildRW().with(sourceResourceId).with(normalized).begin()) {
            txn.linkExistingResource(sourceResourceId, normalized);
            txn.commit();
        }

    }

    @Override
    public void linkPath(final Path source, final Path destination) {

        final var normalizedSrc = normalize(source);
        final var normalizedDst = normalize(destination);

        try (var txn = buildRW().with(normalizedSrc).with(normalizedDst).begin()) {
            final var sourceResourceId = txn.getResourceId(normalizedSrc);
            txn.linkExistingResource(sourceResourceId, normalizedDst);
            txn.commit();
        }

    }

    @Override
    public Unlink unlinkPath(final Path path, final Consumer<Resource> removed) {

        final var context = getContext();
        final var normalized = normalize(path);

        try (var txn = buildRW().with(normalized).begin()) {

            final var unlink = txn.unlinkPath(normalized);

            if (unlink.isRemoved()) {
                final var resource = context.getResident().remove(unlink.getResourceId());
                if (resource != null) removed.accept(resource.getDelegate());
            }

            txn.commit();

            return unlink;

        }

    }

    @Override
    public List<Unlink> unlinkMultiple(final Path path, final int max, final Consumer<Resource> removed) {

        final var context = getContext();
        final var normalized = normalize(path);

        final List<Listing> listings;

        try (final var txn = buildRO().with(normalized).begin()) {
            listings = txn.list(normalized)
                    .limit(max)
                    .collect(toList());
        }

        final var paths = listings.stream().map(Listing::getPath).collect(toList());
        final var resourceIds = listings.stream().map(Listing::getResourceId).collect(toList());

        try (final var txn = buildRW().withPaths(paths).withResourceIds(resourceIds).begin()) {

            final var unlinks = listings.stream()
                    .map(listing -> txn.unlinkPath(listing.getPath()))
                    .collect(toList());

            for(var unlink : unlinks) {

                if (!unlink.isRemoved()) {
                    continue;
                }

                final var resourceId = unlink.getResourceId();
                final var resource = context.getResident().remove(resourceId);

                if (resource != null) {
                    removed.accept(resource.getDelegate());
                }

            }

            txn.commit();
            return unlinks;

        }

    }

    @Override
    public Resource removeResource(final ResourceId resourceId) {
        try (var txn = buildRW().with(resourceId).begin()) {
            txn.removeResource(resourceId);
            txn.commit();
            return getContext().getResident().remove(resourceId);
        }
    }

    @Override
    public List<ResourceId> removeResources(final Path path, final int max, final Consumer<Resource> removed) {

        final var context = getContext();
        final var normalized = normalize(path);

        try (var txn = buildRW().with(path).begin()) {

            final var resourceIDs = txn.removeResources(normalized, max);

            for (final var resourceId : resourceIDs) {

                final var resource = context.getResident().remove(resourceId);

                if (resource != null) {
                    removed.accept(resource);
                }

            }

            return resourceIDs;

        }

    }

    @Override
    public Stream<Resource> removeAllResources() {
        try (var txn = getPersistence().openExclusiveRW()) {

            final var context = this.context.getAndSet(new Context());
            txn.performOperation(ds -> ds.removeAllResources(nodeId));

            return context
                    .getResident()
                    .values()
                    .stream()
                    .map(TransactionalResource::getDelegate);

        }
    }

    @Override
    public long getInMemoryResourceCount() {
        final Context context = getContext();
        return context.getResident().size();
    }

    private Path normalize(final Path path) {
        return path.getOptionalNodeId().isPresent()
                ? path
                : path.toPathWithContext(getNodeId().asString());
    }

    public NodeId getNodeId() {
        return nodeId;
    }

    @Inject
    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public TransactionalResourceServicePersistence getPersistence() {
        return persistence;
    }

    @Inject
    public void setPersistence(final TransactionalResourceServicePersistence persistence) {
        this.persistence = persistence;
    }

    private Context getContext() {
        final Context context = this.context.get();
        if (context == null) throw new IllegalStateException("Not started.");
        return context;
    }

    private Builder<ReadOnlyTransaction> buildRO() {
        return getPersistence().buildRO(getNodeId());
    }

    private Builder<ReadWriteTransaction> buildRW() {
        return getPersistence().buildRW(getNodeId());
    }

    private static class Context {

        private Map<ResourceId, TransactionalResource> resident = new ConcurrentHashMap<>();

        public Map<ResourceId, TransactionalResource> getResident() {
            return resident;
        }

    }

    private class SimpleResourceAcquisition implements ResourceAcquisition {

        private final Context context;

        private final ResourceId resourceId;

        private final TransactionalResource transactionalResource;

        public SimpleResourceAcquisition(Context context, TransactionalResource transactionalResource) {
            this.context = context;
            this.transactionalResource = transactionalResource;
            this.resourceId = transactionalResource.getId();
        }

        @Override
        public ResourceId getResourceId() {
            return resourceId;
        }

        @Override
        public void close() {
            try (var txn = buildRW().with(resourceId).begin()) {
                doRelease(context, txn, transactionalResource);
            }
        }

        @Override
        public ResourceTransaction begin() {
            return acquireWithTransaction(resourceId);
        }

    }

    private class SimpleResourceTransaction implements ResourceTransaction {

        private final Context context;

        private final ReadWriteTransaction txn;

        private final TransactionalResource resource;

        public SimpleResourceTransaction(final ReadWriteTransaction txn, final ResourceId resourceId) {
            try {
                this.txn = txn;
                this.context = getContext();
                this.resource = context.getResident().computeIfAbsent(resourceId, r -> load(txn, resourceId));
            } catch (Exception ex) {
                txn.rollback();
                txn.close();
                throw ex;
            }
        }

        @Override
        public Resource getResource() {
            return resource;
        }

        @Override
        public void commit() {
            doRelease(context, txn, resource);
            txn.commit();
        }

        @Override
        public void rollback() {
            txn.rollback();
        }

        @Override
        public void close() {
            txn.close();
        }

    }

}
