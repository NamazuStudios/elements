package dev.getelements.elements.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import dev.getelements.elements.rt.ResponseCode;
import dev.getelements.elements.rt.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.rt.lua.Constants.HTTP_STATUS_MODULES;

public class HttpStatusBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(ResponseCodeBuiltin.class);

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {

            @Override
            public String getChunkName() {
                return moduleName;
            }

            @Override
            public boolean exists() {
                return HTTP_STATUS_MODULES.contains(moduleName);
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
