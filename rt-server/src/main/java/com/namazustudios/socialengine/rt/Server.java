package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 8/22/15.
 */
public interface Server {

    /**
     * Dispatches the given {@link Request} to the {@link }
     *
     * @param request the request object itself.
     *
     */
    void dispatch(Request request, ResponseReceiver responseReceiver);

}
