package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.InternalException;

import javax.inject.Inject;

public class ContextLocalInvocationDispatcher extends AbstractLocalInvocationDispatcher {

    private Context context;

    @Override
    protected Object resolve(final Class<?> type) {
        if (IndexContext.class.equals(type)) {
            return getContext().getIndexContext();
        } else if (ResourceContext.class.equals(type)) {
            return getContext().getResourceContext();
        } else if (SchedulerContext.class.equals(type)) {
            return getContext().getSchedulerContext();
        } else if (HandlerContext.class.equals(type)) {
            return getContext().getHandlerContext();
        } else {
            throw new InternalException("No dispatch-mapping for type: " + type);
        }
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

}
