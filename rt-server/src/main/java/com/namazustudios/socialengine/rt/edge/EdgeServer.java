package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.*;

import java.util.Map;

/**
 * This is the interface to the underlying server from the "Outside World", the EdgeServer
 * interface is responsible for accepting instances of {@link Request} and negotiating
 * {@link Response} objects.
 *
 * The EdgeServer is responsible for ensuring connections are secure from the outside world.  It may
 * implement a {@link EdgeFilter}, for example, to deny requests from unauthorized users.
 *
 * Note that the EdgeResourceServer is provided a set of boostratp resources.
 *
 * Created by patricktwohig on 8/11/15.
 */
public interface EdgeServer extends Server<EdgeResource> {

    /**
     * The EdgeSErver must have some {@link Resource}s it will add when the server first starts up.  This
     * must be specified as a {@link Map} of stings to {@link Resource} values with the key as the path and the
     * value as the resource at the path.
     */
    String BOOTSTRAP_RESOURCES = "com.namazustudios.socialengine.rt.edge.EdgeServer.BOOTSTRAP_RESOURCES";

    /**
     * Dispatches the given {@link Request} from the {@link EdgeClientSession}.  This method passes
     * the {@link Request} through the various {@link EdgeFilter} instances before
     * it finally arrives at the destination {@link Resource}, or it is handled
     * by the {@link EdgeFilter}.
     *
     * @param request the request object itself.
     * @param edgeClientSession the edgeClientSession making the request
     * @param responseReceiver the {@link ResponseReceiver} instance
     */
    void dispatch(EdgeClientSession edgeClientSession, Request request, ResponseReceiver responseReceiver);

}
