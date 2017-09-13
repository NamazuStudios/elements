package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

import com.naef.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.lua.StackProtector;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YieldInstructionBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(YieldInstructionBuiltin.class);

    public static final String MODULE_NAME = "namazu.yield.instruction";

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
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                final Module module = luaState.checkJavaObject(-1, Module.class);
                logger.info("Loading module {}", module.getChunkName());

                luaState.setTop(0);
                luaState.newTable();

                for (final YieldInstruction yieldInstruction : YieldInstruction.values()) {
                    luaState.pushJavaObject(yieldInstruction);
                    luaState.setField(-2, yieldInstruction.toString());
                }

                return stackProtector.setAbsoluteIndex(1);

            }
        };
    }

}
