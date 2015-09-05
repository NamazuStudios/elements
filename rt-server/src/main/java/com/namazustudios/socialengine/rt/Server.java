package com.namazustudios.socialengine.rt;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by patricktwohig on 8/22/15.
 */
public interface Server {

    /**
     * Subscribes the event receiver to the given path.
     *
     * @param path the path
     * @param name the name of the event
     * @param eventReceiver the receiver
     * @param <PayloadT>
     *
     * @return an instance of {@link Subscription}, which can be used to unsubscribe from the event pool
     * @throws {@link IllegalArgumentException} if the given path is a wildcard path
     *
     */
    <PayloadT> Subscription subscribe(Path path, String name, EventReceiver<PayloadT> eventReceiver);

    /**
     * Subscribes the event receiver to the given paths, recursively.  Note that if the given path is
     * a wildcard path, this will recursively subscribe to all resources matching that
     * particular path.
     *
     * @param path the path
     * @param name the name of the event
     * @param eventReceiver the receiver
     * @param <PayloadT>
     *
     * @return mapping of {@link Path} to {@link Subscription instances , which can be used to unsubscribe from the event pool
     *
     */
    <PayloadT> SortedMap<Path, Subscription> subscribeRecursive(Path path,
                                                                String name,
                                                                EventReceiver<PayloadT> eventReceiver);

}
