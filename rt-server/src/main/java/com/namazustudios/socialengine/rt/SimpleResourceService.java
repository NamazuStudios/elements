package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.edge.EdgeRequestPathHandler;
import com.namazustudios.socialengine.rt.edge.EdgeResource;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by patricktwohig on 8/4/15.
 */
public class SimpleResourceService<ResourceT extends Resource> implements ResourceService<ResourceT> {

    private final ConcurrentMap<List<String>, ResourceT> pathResourceMap =
            new ConcurrentSkipListMap<>(new Comparator<List<String>>() {

                @Override
                public int compare(final List<String> o1, final List<String> o2) {
                    return 0;
                }

            });

    @Override
    public ResourceT getResource(final String path) {

        final List<String> components = EdgeResource.Util.componentsFromPath(path);
        final ResourceT resource = pathResourceMap.get(components);

        if (resource == null) {
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

    }

    @Override
    public void moveResource(final String source, final String destination) {

        final ResourceT resource = getResource(source);
        final List<String> components = Resource.Util.componentsFromPath(source);

        if (pathResourceMap.putIfAbsent(components, resource) == null) {
            pathResourceMap.remove(Resource.Util.componentsFromPath(source));
        } else {
            throw new DuplicateException("Resource at path already exists " + destination);
        }

    }

    @Override
    public ResourceT removeResource(String path) {

        final List<String> components = EdgeResource.Util.componentsFromPath(path);
        final ResourceT resource = pathResourceMap.remove(components);

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
