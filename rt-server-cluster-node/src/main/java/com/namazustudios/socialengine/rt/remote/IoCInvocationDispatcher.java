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
        try {
            final String name = invocation.getName();
            final Class<?> type = Class.forName(invocation.getType());
            final Object object = name == null ? getIocResolver().inject(type) : getIocResolver().inject(type, name);
            doDispatch(object, invocation, invocationResultConsumer);
        } catch (Throwable th) {
            logger.error("Caught exception resolving target for invocation.", th);
            final InvocationResult invocationResult = new InvocationResult();
            invocationResult.setOk(false);
            invocationResult.setThrowable(th);
            invocationResultConsumer.accept(invocationResult);
        }
    }

    private void doDispatch(final Object object,
                            final Invocation invocation,
                            final Consumer<InvocationResult> invocationResultConsumer) {

    }

    public IocResolver getIocResolver() {
        return iocResolver;
    }

    @Inject
    public void setIocResolver(IocResolver iocResolver) {
        this.iocResolver = iocResolver;
    }

}
