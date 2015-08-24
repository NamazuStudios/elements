package com.namazustudios.socialengine.rt;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.edge.EdgeResource;

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

    private final ConcurrentMap<List<String>, ResourceT> pathResourceMap =
            new ConcurrentSkipListMap<>(new Comparator<List<String>>() {

                @Override
                public int compare(final List<String> o1, final List<String> o2) {
                    return o1.size() == o2.size() ? compareEqualLength(o1, o2) : (o1.size() - o2.size());
                }

                private int compareEqualLength(final List<String> o1, final List<String> o2) {

                    final Iterator<String> o1StringIterator = o1.iterator();
                    final Iterator<String> o2StringIterator = o2.iterator();

                    int value = 0;

                    while (o1StringIterator.hasNext() && o2StringIterator.hasNext() && value == 0) {
                        final String s1 = Strings.nullToEmpty(o1StringIterator.next());
                        final String s2 = Strings.nullToEmpty(o2StringIterator.next());
                        value = s1.compareTo(s2);
                    }

                    return value;

                }

            });

    @Inject
    private ResourceLockFactory<ResourceT> lockFactory;

    @Override
    public Iterable<ResourceT> getResources() {

        final Iterable<Map.Entry<List<String>, ResourceT>> entrySet = pathResourceMap.entrySet();

        final Iterable<Map.Entry<List<String>, ResourceT>> resourceIterable =
            Iterables.filter(entrySet, new Predicate<Map.Entry<List<String>, ResourceT>>() {
                @Override
                public boolean apply(final Map.Entry<List<String>, ResourceT> input) {
                    return !lockFactory.isLock(input.getValue());
                }
            });

        return new Iterable<ResourceT>() {
            @Override
            public Iterator<ResourceT> iterator() {

                return new Iterator<ResourceT>() {

                    final Iterator<Map.Entry<List<String>, ResourceT>> wrappedIterator = resourceIterable.iterator();

                    Map.Entry<List<String>, ResourceT> current = wrappedIterator.hasNext() ?
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

                        final List<String> path = current.getKey();
                        final ResourceT resource = current.getValue();

                        if (pathResourceMap.remove(path, resource)) {
                            resource.onRemove(Resource.Util.pathFromComponents(path));
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

    @Override
    public ResourceT getResource(final String path) {
        final List<String> components = EdgeResource.Util.componentsFromPath(path);
        return getResource(components);
    }

    @Override
    public ResourceT getResource(final List<String> pathComponents) {

        final ResourceT resource = pathResourceMap.get(pathComponents);

        if (resource == null) {
            final String path = Resource.Util.pathFromComponents(pathComponents);
            throw new NotFoundException("Resource at path not found: " + path);
        } else if (lockFactory.isLock(resource)) {
            final String path = Resource.Util.pathFromComponents(pathComponents);
            throw new NotFoundException("Resource at path not found: " + path);
        }

        return resource;

    }

    @Override
    public void addResource(final String path, final ResourceT resource) {

        final List<String> components = EdgeResource.Util.componentsFromPath(path);
        final Resource existing = pathResourceMap.putIfAbsent(components, resource);

        if (existing != null) {
            throw new DuplicateException("Resource at path already exists " + path);
        }

        resource.onAdd(Resource.Util.pathFromComponents(components));

    }

    @Override
    public void moveResource(final String source, final String destination) {

        final List<String> sourceComponents = Resource.Util.componentsFromPath(source);
        final List<String> destinationComponents = Resource.Util.componentsFromPath(source);

        // First finds the resource to onMove.  If this does not exist then we
        // just skip over this operation.

        final ResourceT toMove = getResource(sourceComponents);

        try (final ResourceT lock = lockFactory.createLock()) {
            try {

                if (pathResourceMap.put(destinationComponents, lock) != null) {
                    throw new DuplicateException("Resource at path already exists " + destination);
                }

                if (!pathResourceMap.replace(sourceComponents, toMove, lock)) {
                    throw new NotFoundException("Resource at path not found " + source);
                }

                // We have successfully locked both paths.  We can execute the actual
                // onMove operation.  The onMove call should perform any internal operations
                // to onMove the resource, such as pumping events to listeners.

                toMove.onMove(Resource.Util.pathFromComponents(sourceComponents),
                              Resource.Util.pathFromComponents(destinationComponents));

            } catch (RuntimeException ex) {
                throw ex;
            } finally {
                // No matter what happens, this will ensure that the locks are released, but
                // only if we locked in the first place.
                pathResourceMap.remove(destinationComponents, lock);
                pathResourceMap.replace(sourceComponents, lock, toMove);
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
    public ResourceT removeResource(String path) {

        final List<String> components = EdgeResource.Util.componentsFromPath(path);
        final ResourceT resource = pathResourceMap.remove(components);

        if (resource == null || lockFactory.isLock(resource)) {
            throw new NotFoundException("Resource at path not found: " + path);
        }

        resource.onRemove(Resource.Util.pathFromComponents(components));

        return resource;

    }

}
