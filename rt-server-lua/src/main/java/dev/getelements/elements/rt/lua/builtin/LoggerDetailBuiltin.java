package dev.getelements.elements.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import dev.getelements.elements.rt.lua.Constants;
import dev.getelements.elements.rt.lua.persist.ErisPersistence;
import org.slf4j.Logger;

import java.util.function.Supplier;

import static dev.getelements.elements.rt.lua.Constants.LOG_DETAIL_MODULES;

public class LoggerDetailBuiltin implements Builtin {

    private static final String LOGGER = "logger";

    private final JavaFunction logger;

    public LoggerDetailBuiltin(final Supplier<Logger> loggerSupplier) {
         logger = l -> {
            l.pushJavaObject(loggerSupplier.get());
            return 1;
        };
    }

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {
            @Override
            public String getChunkName() {
                return moduleName;
            }

            @Override
            public boolean exists() {
                return LOG_DETAIL_MODULES.contains(moduleName);
            }
        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {
            luaState.newTable();
            luaState.pushJavaFunction(logger);
            luaState.setField(-2, LOGGER);
            return 1;
        };
    }

    @Override
    public void makePersistenceAware(ErisPersistence erisPersistence) {
        erisPersistence.addPermanentJavaObject(logger, LoggerDetailBuiltin.class, LOGGER);
    }

}
