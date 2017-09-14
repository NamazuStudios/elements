package com.namazustudios.socialengine.rt.lua.builtin;

import com.naef.jnlua.JavaFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaObjectBuiltin<T> implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(JavaObjectBuiltin.class);

    private final String moduleName;

    private final T object;

    public JavaObjectBuiltin(final String moduleName, final T t) {
        this.moduleName = moduleName;
        this.object = t;
    }

    @Override
    public Module getModuleNamed(final String name) {
        return new Module() {

            @Override
            public String getChunkName() {
                return moduleName;
            }

            @Override
            public boolean exists() {
                return moduleName.equals(name);
            }

        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {
            final Module module = luaState.checkJavaObject(-1, Module.class);
            logger.info("Loading module {}", module.getChunkName());
            luaState.setTop(0);
            luaState.pushJavaObject(getObject());
            return 1;
        };
    }

    public T getObject() {
        return object;
    }

}
