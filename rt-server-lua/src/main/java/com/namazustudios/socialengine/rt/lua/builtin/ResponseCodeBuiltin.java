package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.ResponseCode;
import com.namazustudios.socialengine.rt.exception.BaseException;
import com.namazustudios.socialengine.rt.lua.LogAssist;
import com.namazustudios.socialengine.rt.lua.persist.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseCodeBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(ResponseCodeBuiltin.class);

    public static final String RESPONSE_CODE_MODULE = "namazu.response.code";

    public static final String EXTRACT = "extract";

    private JavaFunction extract = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);
        luaState.setTop(0);

        try {

            final Exception ex = luaState.toJavaObject(1, Exception.class);

            final ResponseCode responseCode = ex instanceof BaseException ?
                ((BaseException)ex).getResponseCode() :
                ResponseCode.INTERNAL_ERROR_FATAL;

            luaState.pushInteger(responseCode.getCode());
            return 1;

        } catch (Throwable th){
            logAssist.error("Could not start coroutine.", th);
            throw th;
        }

    };

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
            logger.debug("Loading module {} - {}", name, module.getChunkName());

            luaState.setTop(0);
            luaState.newTable();

            for (final ResponseCode responseCode : ResponseCode.values()) {
                luaState.pushInteger(responseCode.getCode());
                luaState.setField(-2, responseCode.toString());
            }

            luaState.pushJavaFunction(extract);
            luaState.setField(-2, EXTRACT);

            return 1;

        };
    }

    @Override
    public void makePersistenceAware(final Persistence persistence) {
        persistence.addPermanentJavaObject(extract, ResponseCodeBuiltin.class, EXTRACT);
    }

}
