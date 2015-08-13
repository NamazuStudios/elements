package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.*;

/**
 * This is the interface to the underlying server from the "Outside World", the EdgeServer
 * interface is responsible for accepting instances of {@link Request} and negotiating
 * {@link Response} objects using the the underlying services such as, {@link ConnectedClientService}
 * and {@link ResourceService}.
 *
 * Additionally, this is responsible for handling any multi-threading and coordinating/driving
 * the calls to {@link EdgeResource#update(double)} calls.
 *
 * The EdgeServer is responsible for ensuring connections are secure from the outside world.  It may
 * implement a {@link EdgeFilter}, for example, to deny requests from unauthorized users.
 *
 * Created by patricktwohig on 8/11/15.
 */
public interface EdgeServer {

    /**
     * Dispatches the given request from the {@link Client}.
     *
     * @param client
     * @param request
     */
    void dispatch(Client client, Request request);

}
