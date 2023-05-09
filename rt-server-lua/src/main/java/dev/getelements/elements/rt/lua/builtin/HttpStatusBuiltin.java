package dev.getelements.elements.rt.lua.builtin;

import dev.getelements.elements.jnlua.JavaFunction;
import dev.getelements.elements.rt.ResponseCode;
import dev.getelements.elements.rt.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpStatusBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(ResponseCodeBuiltin.class);

    public static final String HTTP_STATUS_MODULE = "namazu.http.status";

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {

            @Override
            public String getChunkName() {
                return HTTP_STATUS_MODULE;
            }

            @Override
            public boolean exists() {
                return HTTP_STATUS_MODULE.equals(moduleName);
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

            for (final HttpStatus httpStatus : HttpStatus.values()) {
                luaState.pushInteger(httpStatus.getCode());
                luaState.setField(-2, httpStatus.toString());
            }

            return 1;

        };
    }

}
