package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by patricktwohig on 8/4/15.
 */
public class SimpleResourceService implements ResourceService {

    private final ConcurrentMap<List<String>, Resource> pathResourceMap =
            new ConcurrentSkipListMap<>(new Comparator<List<String>>() {

                @Override
                public int compare(final List<String> o1, final List<String> o2) {
                    return 0;
                }

            });

    @Override
    public Resource getResource(final String path) {

        final List<String> components = Resource.Util.componentsFromPath(path);
        final Resource resource = pathResourceMap.get(components);

        if (resource == null) {
            throw new NotFoundException("Resource at path not found: " + path);
        }

        return resource;

    }

    @Override
    public RequestPathHandler<?> getPathHandler(final RequestHeader requestHeader) {
        final Resource resource = getResource(requestHeader.getPath());
        return resource.getHandler(requestHeader.getMethod());
    }

    @Override
    public <EventT> void subscribe(final String path, final String name, final EventReceiver<EventT> eventReceiver) {
        final Resource resource = getResource(path);
        resource.subscribe(name, eventReceiver);
    }

    @Override
    public <EventT> void unsubscribe(final String path, String name, final EventReceiver<EventT> eventReceiver) {
        final Resource resource = getResource(path);
        resource.unsubscribe(name, eventReceiver);
    }

    @Override
    public void addResource(final String path, final Resource resource) {

        final List<String> components = Resource.Util.componentsFromPath(path);
        final Resource existing = pathResourceMap.putIfAbsent(components, resource);

        if (existing != null) {
            throw new DuplicateException("Resource at path already exists " + path);
        }

    }

    @Override
    public void moveResource(String source, String destination) {
        final Resource resource = removeResource(source);
        addResource(destination, resource);
    }

    @Override
    public Resource removeResource(String path) {

        final List<String> components = Resource.Util.componentsFromPath(path);
        final Resource resource = pathResourceMap.remove(components);

        if (resource == null) {
            throw new NotFoundException("Resource at path not found: " + path);
        }

        return resource;

    }

    @Override
    public void removeAndCloseResource(String path) {
        final Resource resource = removeResource(path);
        resource.close();
    }

}
