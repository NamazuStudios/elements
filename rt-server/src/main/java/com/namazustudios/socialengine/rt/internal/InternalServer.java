package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.Server;
import com.namazustudios.socialengine.rt.edge.EdgeResource;
import com.namazustudios.socialengine.rt.edge.EdgeServer;

/**
 * Used to manage instances of {@link InternalResource}.  In addition to
 *
 * Created by patricktwohig on 8/23/15.
 */
public interface InternalServer extends Server {

    /**
     * Dispatches the given {@link Request} to the {@link Resource} instances contained
     * in this server.  Unlike the {@link EdgeServer}, the {@link Request} is delivered
     * directly to the {@link Resource} for the sake of performance.
     *
     * No filtering is performed, as we assume internal {@link Request}s have been secured
     * through the {@link EdgeResource} business logic.
     *
     * @param request the request object itself.
     *
     */
    void dispatch(Request request, ResponseReceiver responseReceiver);

}
