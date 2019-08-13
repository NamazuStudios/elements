package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.NodeLifecycle;

import javax.inject.Inject;

public class ContextNodeLifecycle implements NodeLifecycle {

    @Override
    public void preStart() {
        getContext().start();
    }

    @Override
    public void postStop() {
        getContext().shutdown();
    }

    private Context context;

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

}
