package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 8/22/15.
 */
public interface Server {

    /**
     * Posts the given event to the server.  This will be received by all
     * {@link EventReceiver} types registered in their appropriate {@link Resource}
     * instances.
     *
     * @param event the event.
     */
    void post(Event event);

    /**
     * Dispatches the given {@link Request} to the {@link }
     *
     * @param request the request object itself.
     *
     */
    void dispatch(Request request, ResponseReceiver responseReceiver);

}
