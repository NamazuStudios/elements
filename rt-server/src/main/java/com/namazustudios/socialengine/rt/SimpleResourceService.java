package com.namazustudios.socialengine.rt;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A generic {@link ResourceService} which can take any type of {@link Resource}.
 *
 * This maps insstances of {@link Resource} to their paths.
 *
 * Created by patricktwohig on 8/4/15.
 */
public class SimpleResourceService<ResourceT extends Resource> implements ResourceService<ResourceT> {

    private final ConcurrentNavigableMap<Path, ResourceT> pathResourceMap = new ConcurrentSkipListMap<>();

    @Inject
    private ResourceLockFactory<ResourceT> lockFactory;

    private final Server server;

    public SimpleResourceService(final Server server) {
        this.server = server;
    }

    @Override
    public Iterable<ResourceT> getResources() {

        final Iterable<Map.Entry<Path, ResourceT>> entrySet = pathResourceMap.entrySet();

        final Iterable<Map.Entry<Path, ResourceT>> resourceIterable =
            Iterables.filter(entrySet, new Predicate<Map.Entry<Path, ResourceT>>() {
                @Override
                public boolean apply(final Map.Entry<Path, ResourceT> input) {
                    return !lockFactory.isLock(input.getValue());
                }
            });

        return new Iterable<ResourceT>() {

            @Override
            public Iterator<ResourceT> iterator() {
                return new ResourceIterator<>(resourceIterable.iterator());
            }

        };

    }

    @Override
    public ResourceT getResource(final Path path) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot fetch single resource with wildcard path " + path);
        }

        final ResourceT resource = pathResourceMap.get(path);

        if (resource == null) {
            throw new NotFoundException("Resource at path not found: " + path);
        } else if (lockFactory.isLock(resource)) {
            throw new NotFoundException("Resource at path not found: " + path);
        }

        return resource;

    }

    @Override
    public Iterable<ResourceT> getResources(final Path path) {
        return new Iterable<ResourceT>() {

            final ConcurrentNavigableMap<Path, ResourceT> tailMap = pathResourceMap.tailMap(path);

            @Override
            public Iterator<ResourceT> iterator() {
                final PeekingIterator<Map.Entry<Path, ResourceT>> peekingIterator =
                    Iterators.peekingIterator(tailMap.entrySet().iterator());

                return new ResourceIterator<ResourceT>(peekingIterator) {
                    @Override
                    public boolean hasNext() {
                        return super.hasNext() && path.matches(peekingIterator.peek().getKey());
                    }
                };

            }
        };
    }

    @Override
    public void addResource(final Path path, final ResourceT resource) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final Resource existing = pathResourceMap.putIfAbsent(path, resource);

        if (existing != null) {
            throw new DuplicateException("Resource at path already exists " + path);
        }

        server.post(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                resource.onAdd(path);
                return null;
            }
        });

    }

    @Override
    public void moveResource(final Path source, final Path destination) {

        if (source.isWildcard() || destination.isWildcard()) {
            throw new IllegalArgumentException("Neither source nor destination may be a wildcard path.");
        }

        // First finds the resource to onMove.  If this does not exist then we
        // just skip over this operation.

        final ResourceT toMove = getResource(source);

        try (final ResourceT lock = lockFactory.createLock()) {
            try {

                if (pathResourceMap.put(destination, lock) != null) {
                    throw new DuplicateException("Resource at path already exists " + destination);
                }

                if (!pathResourceMap.replace(destination, toMove, lock)) {
                    throw new NotFoundException("Resource at path not found " + source);
                }

                // We have successfully locked both paths.  We can execute the actual
                // onMove operation.  The onMove call should perform any internal operations
                // to onMove the resource, such as pumping events to listeners.

                server.post(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        toMove.onMove(source, destination);
                        return null;
                    }
                });

            } catch (RuntimeException ex) {
                throw ex;
            } finally {
                // No matter what happens, this will ensure that the locks are released, but
                // only if we locked in the first place.
                pathResourceMap.remove(destination, lock);
                pathResourceMap.replace(source, lock, toMove);
            }
        }

    }

    @Override
    public void removeAllResources() {

        final Iterator<ResourceT> resourceIterator = getResources().iterator();

        while (resourceIterator.hasNext()) {
            resourceIterator.remove();
        }

    }

    @Override
    public ResourceT removeResource(final Path path) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ResourceT resource = pathResourceMap.remove(path);

        if (resource == null || lockFactory.isLock(resource)) {
            throw new NotFoundException("Resource at path not found: " + path);
        }

        server.post(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                resource.onRemove(path);
                return null;
            }
        });

        return resource;

    }

    private class ResourceIterator<ResourceT extends Resource> implements Iterator<ResourceT> {

        final Iterator<Map.Entry<Path, ResourceT>> wrappedIterator;

        private Map.Entry<Path, ResourceT> last = null;

        public ResourceIterator(Iterator<Map.Entry<Path, ResourceT>> wrappedIterator) {
            this.wrappedIterator = wrappedIterator;
        }

        @Override
        public boolean hasNext() {
            return wrappedIterator.hasNext();
        }

        @Override
        public ResourceT next() {
            return (last = wrappedIterator.next()).getValue();
        }

        @Override
        public void remove() {

            if (last == null) {
                throw new IllegalStateException();
            }

            final Path key = last.getKey();
            final ResourceT value = last.getValue();

            if (pathResourceMap.remove(key, value)) {
                value.onRemove(key);
            }

        }

    }

}
