package com.namazustudios.socialengine.rt;

import java.util.Set;
import java.util.function.Consumer;

public interface Listenable<T extends Listenable.Listener> {
    void registerListener(T listener);
    boolean unregisterListener(T listener);

    Set<? extends Listener> getListeners();

    interface Listener {}
}
