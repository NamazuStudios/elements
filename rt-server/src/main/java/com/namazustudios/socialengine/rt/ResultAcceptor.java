package com.namazustudios.socialengine.rt;

import java.util.function.Consumer;

/**
 * Provides a means to accept results returned by instances of {@link MethodDispatcher}
 */
public interface ResultAcceptor<T> {

    /**
     *
     * @param tConsumer
     */
    void withConsumer(Consumer<T> tConsumer);

}
