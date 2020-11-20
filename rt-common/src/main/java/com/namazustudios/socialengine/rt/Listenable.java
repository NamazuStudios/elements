package com.namazustudios.socialengine.rt;

import java.util.Set;

public interface Listenable<T extends Listenable.Listener> {
    void registerListener(T listener);

    boolean unregisterListener(T listener);

    Set<T> getListeners();

    interface Listener {}
}
