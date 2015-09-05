package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 9/4/15.
 */
public interface ClientRequestDispatcher {

    /**
     * This will dispatch the given {@link Request} over the network.
     *
     * @param request
     */
    void dispatch(final Request request);

}
