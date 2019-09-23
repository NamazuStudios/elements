package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.NodeLifecycle;

import javax.inject.Inject;
import javax.inject.Named;

import static com.namazustudios.socialengine.rt.Context.LOCAL;

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
    public void setContext(@Named(LOCAL) Context context) {
        this.context = context;
    }

}
