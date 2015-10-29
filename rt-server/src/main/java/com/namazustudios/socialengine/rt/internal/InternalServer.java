package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.EdgeServer;

/**
 * Used to manage instances of {@link InternalResource}.  In addition to
 *
 * Created by patricktwohig on 8/23/15.
 */
public interface InternalServer extends Server<InternalResource> {

    /**
     * Retains the {@link Resource} at the given {@link Path}, and returns the instance.
     *
     * {@link {@see InternalResource#retain()}}
     *
     * @param path the path
     * @return the {@link InternalResource} at the given {@link Path}
     * @throws {@link NotFoundException} if the resource can't be found, or the resource has been slated for destruction
     */
    InternalResource retain(Path path);

    /**
     * Retains the {@link InternalResource} at the given {@link Path}.  If the {@link InternalResource} does not
     * exist, then the given {@link ResourceInitializer} will be used to obtain the fully initialized
     * instance.
     *
     * This operation is completed atomically.  Upon return this guarantees taht the existing resource will have
     * its reference count incremented by one, or it will insert the new resource using the given resource
     * initializer.
     *
     * {@see {@link ResourceService#addResource(Path, Resource)}}
     *
     * @param path the path
     * @param resourceInitializer a {@link ResourceInitializer} instnace which will provide the instance if necessary
     * @return the existing {@link InternalServer} (with its refcount incremented)
     */
    InternalResource retainOrAddResourceIfAbsent(Path path, ResourceInitializer<InternalResource> resourceInitializer);

    /**
     * Dispatches the given {@link Request} to the {@link Resource} instances contained
     * in this server.  Unlike the {@link EdgeServer}, the {@link Request} is delivered
     * directly to the {@link Resource} for the sake of performance.
     *
     * No filtering is performed, as we assume internal {@link Request}s have been secured
     * through the {@link InternalResource} business logic.
     *
     * @param request the request object itself.
     *
     */
    void dispatch(Request request, ResponseReceiver responseReceiver);

    /**
     * Adds {@link Resource} to this instance, as well as any additional steps required by the server.
     *
     * {@see {@link ResourceService#addResource(Path, Resource)}}
     *
     * @param path the path
     * @param resource the resource
     */
    void addResource(Path path, InternalResource resource);

    /**
     * Moves the {@link Resource} on this server, as well as any additional steps required by the server.
     *
     * {@see {@link ResourceService#moveResource(Path, Path)}}
     *
     * @param source the source path
     * @param destination the destination path
     */
    void moveResource(Path source, Path destination);

    /**
     * Moves the {@link Resource} on this server, as well as any additional steps required by the server.
     *
     * {@see {@link ResourceService#removeAllResources()}
     *
     * @param source the source path
     * @param destination the destination path
     */
    void removeAllResources();

    /**
     * Adds {@link Resource} from this instance, as well as any additional steps required by the server.
     *
     * {@see {@link ResourceService#removeResource(Path)}}
     *
     * @param path the path
     */
    InternalResource removeResource(Path path);

}
