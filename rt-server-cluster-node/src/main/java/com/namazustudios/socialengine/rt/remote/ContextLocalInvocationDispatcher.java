package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.InternalException;

import javax.inject.Inject;
import javax.inject.Named;

import static com.namazustudios.socialengine.rt.Context.LOCAL;

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
        } else if (TaskContext.class.equals(type)) {
            return getContext().getTaskContext();
        } else {
            throw new InternalException("No dispatch-mapping for type: " + type);
        }
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(@Named(LOCAL) Context context) {
        this.context = context;
    }

}
