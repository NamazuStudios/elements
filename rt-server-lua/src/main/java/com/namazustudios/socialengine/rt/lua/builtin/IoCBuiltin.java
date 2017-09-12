package com.namazustudios.socialengine.rt.lua.builtin;

import com.naef.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.lua.IocResolver;

public class IoCBuiltin implements Builtin {

    public static final String MODULE_NAME = "namazu.ioc";

    private final IocResolver iocResolver;

    public IoCBuiltin(IocResolver iocResolver) {
        this.iocResolver = iocResolver;
    }

    @Override
    public Module getModuleNamed(final String name) {
        return null;
    }

    @Override
    public JavaFunction getSearcher() {
        return null;
    }

    @Override
    public JavaFunction getGetLoader() {
        return null;
    }

}
