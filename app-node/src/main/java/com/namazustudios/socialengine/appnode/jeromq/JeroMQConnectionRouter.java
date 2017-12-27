package com.namazustudios.socialengine.appnode.jeromq;

import com.namazustudios.socialengine.appnode.ConnectionRouter;

import java.util.concurrent.atomic.AtomicReference;

public class JeroMQConnectionRouter implements ConnectionRouter {

    private final AtomicReference<Thread> routerThread = new AtomicReference<>();

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

}
