package dev.getelements.elements.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.LuaState;
import dev.getelements.elements.rt.lua.Constants;
import dev.getelements.elements.rt.lua.LogAssist;
import dev.getelements.elements.rt.lua.persist.ErisPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Manges instances of {@link Builtin} as it interacts with an instance of {@link LuaState}.
 */
public class BuiltinManager {

    private static final Logger logger = LoggerFactory.getLogger(BuiltinManager.class);

    private final LogAssist logAssist;

    private final Supplier<LuaState> luaStateSupplier;

    private Consumer<Builtin> handlePersistence;

    public BuiltinManager(final Supplier<LuaState> luaStateSupplier, final Supplier<Logger> loggerSupplier) {
        this.luaStateSupplier = luaStateSupplier;
        logAssist = new LogAssist(loggerSupplier, luaStateSupplier);
        handlePersistence = b -> {};
    }

    public BuiltinManager(final Supplier<LuaState> luaStateSupplier,
                          final Supplier<Logger> loggerSupplier,
                          final ErisPersistence erisPersistence) {
        this.luaStateSupplier = luaStateSupplier;
        logAssist = new LogAssist(loggerSupplier, luaStateSupplier);
        handlePersistence = b -> b.makePersistenceAware(erisPersistence);
    }

    /**
     * Installs the {@link Builtin} module to this {@link BuiltinManager} such that the underlying code may make use of
     * it using the require function.
     *
     * @param builtin the {@link Builtin} to install
     */
    public void installBuiltin(final Builtin builtin) {

        final LuaState luaState = luaStateSupplier.get();
        handlePersistence.accept(builtin);

        try {

            logger.debug("Installing builtin {}", builtin);

            luaState.getGlobal(Constants.PACKAGE_TABLE);
            luaState.getField(-1, Constants.PACKAGE_SEARCHERS_TABLE);

            final int index = luaState.rawLen(-1) + 1;
            luaState.pushJavaFunction(builtin.getSearcher());
            luaState.rawSet(-2, index);

        } catch (final Throwable th){
            logAssist.error("Failed to install builtin: " + builtin, th);
            throw th;
        } finally {
            luaState.setTop(0);
        }

    }

}
