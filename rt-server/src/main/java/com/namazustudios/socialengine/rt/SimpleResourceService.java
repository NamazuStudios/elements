package com.namazustudios.socialengine.rt;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.sun.tools.corba.se.idl.constExpr.Not;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A generic {@link ResourceService} which can take any type of {@link Resource}.
 *
 * This maps instances of {@link Resource} to their paths.
 *
 * This uses a simple locking strategy to lock paths in order to manipulate resources.  Note that the
 * entire resource isn't locked, but rather paths are locked separately.  Everything is stored
 * in memory.
 *
 * Note that a {@link Resource} must exist at one and only one {@link Path}.
 *
 * Created by patricktwohig on 8/4/15.
 */
public class SimpleResourceService<ResourceT extends Resource> implements ResourceService<ResourceT> {

    private final ConcurrentNavigableMap<Path, ResourceT> pathResourceMap = new ConcurrentSkipListMap<>();

    private final Server server;

    private ResourceLockFactory<ResourceT> lockFactory;

    @Inject
    public SimpleResourceService(final Server server,
                                 final ResourceLockFactory<ResourceT> lockFactory) {
        this.server = server;
        this.lockFactory = lockFactory;
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
    public AtomicOperationTuple<ResourceT> addResourceIfAbsent(
            final Path path, final ResourceInitializer<ResourceT> resourceInitializer) {

        // Creates a lock, which will be the substitute for the object until we
        // figure out if we need to create it, or if we need to insert it.  In any
        // case, we need

        try (final ResourceT lock = lockFactory.createLock()) {
            try {

                // Locks the path.  This ensures that we will be able to replace the object
                // by creating the instance only once.  The initialization of the object
                // may be a heavy operation.  Therefore, we want to leave the initialization
                // of the object
                final ResourceT existing = pathResourceMap.putIfAbsent(path, lock);

                if (existing == null) {

                    // We must instantiate the resource afresh.  The initializer will return
                    // the instance new, and we can easily just replace the lock with the
                    // resource.

                    final ResourceT toPut = resourceInitializer.init();

                    if (!pathResourceMap.replace(path, lock, toPut)) {
                        // This shouldn't happen, but if it does something is definitely not
                        // working correctly.  To be safe, we throw an exeption here to hopefully
                        // expose bugs in the implementation (if necessary)
                        throw new InternalException("could not unlock resource at path " + path);
                    }

                    server.post(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            toPut.onAdd(path);
                            return null;
                        }
                    });

                    return new AtomicOperationTuple<ResourceT>() {
                        @Override
                        public boolean isNewlyAdded() {
                            return true;
                        }

                        @Override
                        public ResourceT getResource() {
                            return toPut;
                        }
                    };

                } else {

                    // The put failed because something already.  Return the existing resource
                    // and the false flag to indicate that we did not initialize
                    // the resource.

                    return new AtomicOperationTuple<ResourceT>() {
                        @Override
                        public boolean isNewlyAdded() {
                            return false;
                        }

                        @Override
                        public ResourceT getResource() {
                            return existing;
                        }
                    };

                }

            } finally {
                // Roll the locking operation back out, if it is there.  If it is not
                // the lock then this operation is a no-op.  This is added to the
                // finally block to ensure that the lock is always removed if necessary
                pathResourceMap.remove(path, lock);
            }
        }

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

                if (pathResourceMap.putIfAbsent(destination, lock) != null) {
                    // Lock the destination.  If the destination already occupied, then this
                    // will fail right here.  NOthing else needs to happen.
                    throw new DuplicateException("Resource at path already exists " + destination);
                }

                if (!pathResourceMap.replace(source, toMove, lock)) {
                    // We try to put the lock in the source.  If the resource is still the same
                    // in the source this check won't fail.  If the check failed, then somebody
                    // else moved it while we were securing the destination.  The lock is placed
                    // here so we can roll everything back if needed.
                    throw new NotFoundException("Resource not found " + source);
                }

                // Finally, we have a lock in both the source and destination.  THis means that
                // the final step should be to simply replace the lock with the object.  This
                // step should not fail.  But we add a check to ensure consistency.  This may be
                // happening if either object's equals/hashcode method is broken.

                if (!pathResourceMap.replace(destination, lock, toMove)) {
                    // Attempt to replace the lock with the object we want to move.  This way
                    // we are sure that we are the only ones changing the object at that path.
                    throw new InternalException("Could not move resource to destination " + destination);
                }

                // We have successfully locked both paths.  We can execute the actual
                // onMove operation.  The onMove call should perform any internal operations
                // to onMove the resource, such as pumping events to listeners.  Though technically
                // the resource will be living in two places right now, this will be removed
                // shortly from its source path

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
                // only if we locked in the first place.  Regardless of state, all outcomes
                // should place the map in a consistent state.

                pathResourceMap.remove(destination, lock);

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

    @Override
    public void removeAndCloseResource(final Path path) {
        final ResourceT resource = removeResource(path);
        resource.close();
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
