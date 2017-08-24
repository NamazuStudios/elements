package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaException;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.exception.BadManifestException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class LuaManifestLoader implements ManifestLoader {

    public static final String MAIN_MANIFEST = "main.lua";

    public static final String MANIFEST_TABLE = "manifest";

    public static final String MODEL_TABLE = "model";

    public static final String HTTP_TABLE = "http";

    private static final Logger logger = LoggerFactory.getLogger(LuaManifestLoader.class);

    private static final Logger scriptLogger = LoggerFactory.getLogger(MAIN_MANIFEST);

    private static final JavaFunction print = new ScriptLogger(s -> scriptLogger.info("{}", s));

    private AssetLoader assetLoader;

    private Provider<LuaState> luaStateProvider;

    private final Object lock = new Object();

    private boolean closed = false;

    private LuaState luaState = null;

    @Override
    public ModelManifest getModelManifest() {
        return loadIfNecessaryAndFetchFromManifestTable(MODEL_TABLE, ModelManifest.class);
    }

    @Override
    public HttpManifest getHttpManifest() {
        return loadIfNecessaryAndFetchFromManifestTable(HTTP_TABLE, HttpManifest.class);
    }

    @Override
    public void close() {
        synchronized (lock) {

            if (closed) {
                throw new IllegalStateException();
            }

            if (luaState != null) {
                luaState.close();
            }

            closed = true;
            getAssetLoader().close();

        }
    }

    private <T> T loadIfNecessaryAndFetchFromManifestTable(final String table, Class<T> tClass) {
        synchronized (lock) {
            loadAndRunIfNecessary();
            return fromManifestTable(table, tClass);
        }
    }

    private void loadAndRunIfNecessary() {
        if (closed) {
            throw new IllegalStateException("already closed");
        } else if (luaState == null) {
            try (final InputStream inputStream = getAssetLoader().open(MAIN_MANIFEST)) {

                luaState = getLuaStateProvider().get();

                createManifestTables();
                setupFunctionOverrides();

                luaState.load(inputStream, MAIN_MANIFEST, "bt");
                scriptLogger.info("Loaded Script: {}", MAIN_MANIFEST);

                luaState.call(0, 0);
                scriptLogger.info("Executed Script: {}", MAIN_MANIFEST);

            } catch (IOException ex) {
                logger.error("Caught IO exception reading manifest {}.", MAIN_MANIFEST, ex);
                throw new InternalException(ex);
            } catch (LuaException ex) {
                logger.error("Possible malformed manifest: {}.", MAIN_MANIFEST, ex);
                throw new BadManifestException(ex);
            }
        }
    }

    private void setupFunctionOverrides() {
        luaState.pushJavaFunction(print);
        luaState.setGlobal(Constants.PRINT_FUNCTION);
    }

    private void createManifestTables() {
        try (final StackProtector s = new StackProtector(luaState)) {

            // manifest
            luaState.newTable();

            // manifest.http
            luaState.newTable();
            luaState.setField(-2, HTTP_TABLE);

            // manifest.model
            luaState.newTable();
            luaState.setField(-2, MODEL_TABLE);

            // Sets manifest to global table.
            luaState.setGlobal(MANIFEST_TABLE);

        }
    }

    private <T> T fromManifestTable(final String table, final Class<T> tClass) {
        try (final StackProtector s = new StackProtector(luaState)){
            luaState.getGlobal(MANIFEST_TABLE);
            luaState.getField(-1, table);
            return luaState.toJavaObject(-1, tClass);
        } catch (ClassCastException | LuaException ex) {
            logger.error("Caught exception reading manifest {}.", MAIN_MANIFEST, ex);
            throw new BadManifestException(ex);
        }
    }

    public AssetLoader getAssetLoader() {
        return assetLoader;
    }

    @Inject
    public void setAssetLoader(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    public Provider<LuaState> getLuaStateProvider() {
        return luaStateProvider;
    }

    @Inject
    public void setLuaStateProvider(Provider<LuaState> luaStateProvider) {
        this.luaStateProvider = luaStateProvider;
    }

}
