package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.IocResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.function.Consumer;

public class IoCInvocationDispatcher implements InvocationDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(IoCInvocationDispatcher.class);

    private IocResolver iocResolver;

    @Override
    public void dispatch(final Invocation invocation, final Consumer<InvocationResult> invocationResultConsumer) {

        final Object object;

        try {
            final String type = invocation.getType();
            final String name = invocation.getName();
            object = name == null ? getIocResolver().inject(type) : getIocResolver().inject(type, name);
        } catch (Throwable th) {
            logger.error("Caught exception resolving target for invocation.", th);
            final InvocationResult invocationResult = new InvocationResult();
            invocationResult.setOk(false);
            invocationResult.setThrowable(th);
            invocationResultConsumer.accept(invocationResult);
            return;
        }

    }

    public IocResolver getIocResolver() {
        return iocResolver;
    }

    @Inject
    public void setIocResolver(IocResolver iocResolver) {
        this.iocResolver = iocResolver;
    }

}
