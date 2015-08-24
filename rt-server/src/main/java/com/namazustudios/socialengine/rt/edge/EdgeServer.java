package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.*;

/**
 * This is the interface to the underlying server from the "Outside World", the EdgeServer
 * interface is responsible for accepting instances of {@link Request} and negotiating
 * {@link Response} objects using the the underlying services such as, {@link ConnectedEdgeClientService}
 * and {@link ResourceService}.
 *
 * Additionally, this is responsible for handling any multi-threading and coordinating/driving
 * the calls to {@link Resource#onUpdate()} calls.
 *
 * The EdgeServer is responsible for ensuring connections are secure from the outside world.  It may
 * implement a {@link EdgeFilter}, for example, to deny requests from unauthorized users.
 *
 * Note that the EdgeResourceServer is provided a set of boostratp resources.
 *
 * Created by patricktwohig on 8/11/15.
 */
public interface EdgeServer extends Server {

    /**
     * Dispatches the given {@link Request} from the {@link EdgeClient}.  This method passes
     * the {@link Request} through the various {@link EdgeFilter} instances before
     * it finally arrives at the destination {@link Resource}, or it is handled
     * by the {@link EdgeFilter}.
     *
     * @param request the request object itself.
     * @param edgeClient the edgeClient making the request
     * @param responseReceiver the {@link ResponseReceiver} instance
     */
    void dispatch(EdgeClient edgeClient, Request request, ResponseReceiver responseReceiver);

}
