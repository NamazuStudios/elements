package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 8/22/15.
 */
public interface Server {

    /**
     * Subscribes the event receiver to the given path.  Note that if the given path is
     * a wildcard path, this will recursively subscribe to all resources matching that
     * particular path.
     *
     * @param path the path
     * @param name the name of the event
     * @param eventReceiver the receiver
     * @param <PayloadT>
     *
     * @return an instance of {@link Subscription}, which can be used to unsubscribe from the event pool
     *
     */
    <PayloadT> Subscription subscribe(Path path, String name, EventReceiver<PayloadT> eventReceiver);

}
