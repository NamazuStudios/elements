package com.namazustudios.socialengine.rt.lua.builtin;

import com.naef.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.ResponseCode;
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

            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);
            logger.info("Loading module {} - {}", name, module.getChunkName());

            luaState.setTop(0);
            luaState.newTable();

            for (final ResponseCode responseCode : ResponseCode.values()) {
                luaState.pushString(responseCode.toString());
                luaState.setField(-2, responseCode.toString());
            }

            return 1;

        };
    }

}
