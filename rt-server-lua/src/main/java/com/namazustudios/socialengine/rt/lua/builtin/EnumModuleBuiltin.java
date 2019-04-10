package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumModuleBuiltin<T extends Enum<T>> implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(EnumModuleBuiltin.class);

    private final Class<T> enumClass;

    private final String moduleName;

    public EnumModuleBuiltin(Class<T> enumClass, final String moduleName) {
        this.enumClass = enumClass;
        this.moduleName = moduleName;
    }

    @Override
    public Module getModuleNamed(final String moduleName) {
        final String enumModuleName = this.moduleName;

        return new Module() {

            @Override
            public String getChunkName() {
                return enumModuleName;
            }

            @Override
            public boolean exists() {
                return enumModuleName.equals(moduleName);
            }

        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {

            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);
            logger.debug("Loading module {} - {}", name, module.getChunkName());

            // build a lua table of the form {enumElement.toString(): enumElement}
            luaState.setTop(0);
            luaState.newTable();

            for (final Enum<T> enumValue : enumClass.getEnumConstants()) {
                final String enumValueString = enumValue.toString();
                luaState.pushJavaObject(enumValue);
                luaState.setField(-2, enumValueString);
            }

            return 1;

        };
    }

}
