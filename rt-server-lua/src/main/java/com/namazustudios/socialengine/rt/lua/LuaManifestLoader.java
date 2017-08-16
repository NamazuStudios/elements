package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaException;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.exception.BadManifestException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class LuaManifestLoader implements ManifestLoader {

    private static final Logger logger = LoggerFactory.getLogger(LuaManifestLoader.class);

    public static final String MAIN_MANIFEST = "main.lua";

    public static final String MANIFEST_TABLE = "manifest";

    public static final String MODEL_TABLE = "model";

    public static final String HTTP_TABLE = "http";

    private AssetLoader assetLoader;

    private Provider<LuaState> luaStateProvider;

    private Mapper dozerMapper;

    private final Object lock = new Object();

    private final AtomicReference<HttpManifest> httpManifestAtomicReference = new AtomicReference<>();

    private final AtomicReference<ModelManifest> modelManifestAtomicReference = new AtomicReference<>();

    private boolean closed = false;

    private LuaState luaState = null;

    @Override
    public ModelManifest getModelManifest() {
        final ModelManifest modelManifest = modelManifestAtomicReference.get();
        return modelManifest == null ? loadIfNecessary(modelManifestAtomicReference, ModelManifest.class, MODEL_TABLE) : modelManifest;
    }

    @Override
    public HttpManifest getHttpManifest() {
        final HttpManifest httpManifest = httpManifestAtomicReference.get();
        return httpManifest == null ? loadIfNecessary(httpManifestAtomicReference, HttpManifest.class, HTTP_TABLE) : httpManifest;
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

        }
    }

    private <T> T loadIfNecessary(final AtomicReference<T> tAtomicReference, final Class<T> tClass, final String table) {
        synchronized (lock) {

            if (closed) {
                throw new IllegalStateException("already closed");
            }

            T t = tAtomicReference.get();

            if (t == null) {
                t = load(tClass, table);
                tAtomicReference.compareAndSet(null, t);
            }

            return t;

        }
    }

    private <T> T load(final Class<T> tClass, String table) {
        loadStateIfNecessary();
        final Map<?, ?> tableMap = mapFromTable(table);
        return getDozerMapper().map(tableMap, tClass);
    }

    private void loadStateIfNecessary() {
        if (luaState == null) {
            try (final InputStream inputStream = getAssetLoader().open(MAIN_MANIFEST)) {
                luaState = getLuaStateProvider().get();
                createManifestTables();
                luaState.load(inputStream, MAIN_MANIFEST, "bt");
            } catch (IOException ex) {
                logger.error("Caught IO exception reading manifest {}.", MAIN_MANIFEST, ex);
                throw new InternalException(ex);
            } catch (LuaException ex) {
                logger.error("Possible malformed manifest: {}.", MAIN_MANIFEST, ex);
                throw new BadManifestException(ex);
            }
        }
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

    private Map<?, ?> mapFromTable(final String table) {
        try (final StackProtector s = new StackProtector(luaState)){
            luaState.getGlobal(MANIFEST_TABLE);
            luaState.getField(-1, table);
            return luaState.toJavaObject(-1, Map.class);
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

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

}
