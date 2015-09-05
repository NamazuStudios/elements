package com.namazustudios.socialengine.rt;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A generic {@link ResourceService} which can take any type of {@link Resource}.
 *
 * This maps insstances of {@link Resource} to their paths.
 *
 * Created by patricktwohig on 8/4/15.
 */
public class SimpleResourceService<ResourceT extends Resource> implements ResourceService<ResourceT> {

    private final ConcurrentMap<Path, ResourceT> pathResourceMap = new ConcurrentSkipListMap<>();

    @Inject
    private ResourceLockFactory<ResourceT> lockFactory;

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

                return new Iterator<ResourceT>() {

                    final Iterator<Map.Entry<Path, ResourceT>> wrappedIterator = resourceIterable.iterator();

                    Map.Entry<Path, ResourceT> current = wrappedIterator.hasNext() ?
                                                                 wrappedIterator.next() : null;

                    @Override
                    public boolean hasNext() {
                        return current != null;
                    }

                    @Override
                    public ResourceT next() {

                        if (!hasNext()) {
                            throw new IllegalStateException();
                        }

                        try {
                            return current.getValue();
                        } finally {
                            advance();
                        }

                    }

                    @Override
                    public void remove() {

                        if (!hasNext()) {
                            throw new IllegalStateException();
                        }

                        final Path path = current.getKey();
                        final ResourceT resource = current.getValue();

                        if (pathResourceMap.remove(path, resource)) {
                            resource.onRemove(path);
                        }

                        advance();

                    }

                    private void advance() {
                        current = wrappedIterator.hasNext() ?
                                  wrappedIterator.next() : null;
                    }

                };
            }
        };

    }

//    @Override
//    public ResourceT getResource(final String path) {
//        final List<String> components = Path.Util.componentsFromPath(path);
//        return getResource(components);
//    }

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
    public void addResource(final Path path, final ResourceT resource) {

        final Resource existing = pathResourceMap.putIfAbsent(path, resource);

        if (existing != null) {
            throw new DuplicateException("Resource at path already exists " + path);
        }

        resource.onAdd(path);

    }

    @Override
    public void moveResource(final Path source, final Path destination) {

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

                toMove.onMove(source, destination);

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

        final ResourceT resource = pathResourceMap.remove(path);

        if (resource == null || lockFactory.isLock(resource)) {
            throw new NotFoundException("Resource at path not found: " + path);
        }

        resource.onRemove(path);

        return resource;

    }

}
