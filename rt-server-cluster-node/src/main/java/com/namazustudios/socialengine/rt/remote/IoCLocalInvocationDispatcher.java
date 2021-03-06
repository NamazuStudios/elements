package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.IocResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class IoCLocalInvocationDispatcher extends AbstractLocalInvocationDispatcher {

    private IocResolver iocResolver;

    @Override
    protected Object resolve(final Class<?> type) {
        return getIocResolver().inject(type);
    }

    @Override
    protected Object resolve(final Class<?> type, final String name) {
        return getIocResolver().inject(type, name);
    }

    public IocResolver getIocResolver() {
        return iocResolver;
    }

    @Inject
    public void setIocResolver(IocResolver iocResolver) {
        this.iocResolver = iocResolver;
    }

}
