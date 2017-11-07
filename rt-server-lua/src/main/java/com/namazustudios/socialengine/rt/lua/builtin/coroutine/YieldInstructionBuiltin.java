package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YieldInstructionBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(YieldInstructionBuiltin.class);

    public static final String MODULE_NAME = CoroutineBuiltin.MODULE_NAME + ".yieldinstruction";

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {
            @Override
            public String getChunkName() {
                return MODULE_NAME;
            }

            @Override
            public boolean exists() {
                return MODULE_NAME.equals(moduleName);
            }
        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {

            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);
            logger.info("Loading module {} - {}", name, module.getChunkName());

            luaState.setTop(0);
            luaState.newTable();

            for (final YieldInstruction yieldInstruction : YieldInstruction.values()) {
                luaState.pushJavaObject(yieldInstruction.toString());
                luaState.setField(-2, yieldInstruction.toString());
            }

            return 1;

        };
    }

}
