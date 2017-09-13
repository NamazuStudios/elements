package com.namazustudios.socialengine.rt.lua.builtin;

import com.naef.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.ResponseCode;
import com.namazustudios.socialengine.rt.lua.StackProtector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseCodeBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(ResponseCodeBuiltin.class);

    public static final String RESPONSE_CODE_MODULE = "namazu.response.code";

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {

            @Override
            public String getChunkName() {
                return RESPONSE_CODE_MODULE;
            }

            @Override
            public boolean exists() {
                return RESPONSE_CODE_MODULE.equals(moduleName);
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

                for (final ResponseCode responseCode : ResponseCode.values()) {
                    luaState.pushInteger(responseCode.getCode());
                    luaState.setField(-2, responseCode.toString());
                }

                return stackProtector.setAbsoluteIndex(1);

            }

        };
    }

}
