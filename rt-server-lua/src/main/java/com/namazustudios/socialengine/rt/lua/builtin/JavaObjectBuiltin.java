package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.lua.persist.ErisPersistence;
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
            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);
            logger.debug("Loading module {} - {}", name, module.getChunkName());
            luaState.setTop(0);
            luaState.pushJavaObject(getObject());
            return 1;
        };
    }

    @Override
    public void makePersistenceAware(final ErisPersistence erisPersistence) {
        erisPersistence.addPermanentJavaObject(object, JavaObjectBuiltin.class, moduleName);
    }

    public T getObject() {
        return object;
    }

}
