package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.exception.InternalException;

import javax.inject.Inject;
import javax.inject.Named;

import static dev.getelements.elements.rt.Context.LOCAL;
import static dev.getelements.elements.rt.Context.REMOTE;

public class ContextLocalInvocationDispatcher extends AbstractLocalInvocationDispatcher {

    private Context context;

    @Override
    protected Object resolve(final Class<?> type, final String name) {
        if (!REMOTE.equals(name)) {
            throw new InternalException("No dispatch-mapping for type: " + type + "name: " + name);
        } else if (IndexContext.class.equals(type)) {
            return getContext().getIndexContext();
        } else if (ResourceContext.class.equals(type)) {
            return getContext().getResourceContext();
        } else if (SchedulerContext.class.equals(type)) {
            return getContext().getSchedulerContext();
        } else if (HandlerContext.class.equals(type)) {
            return getContext().getHandlerContext();
        } else if (TaskContext.class.equals(type)) {
            return getContext().getTaskContext();
        } else if (ManifestContext.class.equals(type)) {
            return getContext().getManifestContext();
        } else if (EventContext.class.equals(type)) {
            return getContext().getEventContext();
        } else {
            throw new InternalException("No dispatch-mapping for type: " + type + "name: " + name);
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
