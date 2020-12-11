package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.LuaException;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.exception.BadManifestException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.lua.builtin.*;
import com.namazustudios.socialengine.rt.manifest.event.EventManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.security.SecurityManifest;
import com.namazustudios.socialengine.rt.manifest.startup.StartupManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;

import static com.namazustudios.socialengine.rt.Attributes.emptyAttributes;
import static com.namazustudios.socialengine.rt.lua.Constants.ATTRIBUTES_MODULE;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class LuaManifestLoader implements ManifestLoader {

    public static final String MAIN_MANIFEST = "main.lua";

    public static final String MANIFEST_TABLE = "manifest";

    public static final String MODEL_TABLE = "model";

    public static final String SECURITY_TABLE = "security";

    public static final String HTTP_TABLE = "http";

    public static final String STARTUP_TABLE = "startup";

    public static final String EVENT_TABLE = "event";

    private static final Logger logger = LoggerFactory.getLogger(LuaManifestLoader.class);

    private static final Logger scriptLogger = LoggerFactory.getLogger(MAIN_MANIFEST);

    private static final JavaFunction print = new ScriptLogger(s -> scriptLogger.info("{}", s));

    private ModelManifest modelManifest;

    private HttpManifest httpManifest;

    private SecurityManifest securityManifest;

    private StartupManifest startupManifest;

    private EventManifest eventManifest;

    private AssetLoader assetLoader;

    private Provider<LuaState> luaStateProvider;

    private Provider<AssetLoaderBuiltin> assetLoaderBuiltinProvider;

    private Provider<ClasspathBuiltin> classpathBuiltinProvider;

    private final Object lock = new Object();

    private boolean closed = false;

    private LuaState luaState = null;

    private final BuiltinManager builtinManager = new BuiltinManager(() -> luaState, () -> scriptLogger);

    @Override
    public boolean getClosed() {
        return closed;
    }

    private void setModelManifest(ModelManifest modelManifest) {
        this.modelManifest = modelManifest;
    }

    @Override
    public ModelManifest getModelManifest() {
        loadAndRunIfNecessary();
        return modelManifest;
    }

    private void setHttpManifest(HttpManifest httpManifest) {
        this.httpManifest = httpManifest;
    }

    @Override
    public HttpManifest getHttpManifest() {
        loadAndRunIfNecessary();
        return httpManifest;
    }

    private void setSecurityManifest(SecurityManifest securityManifest) {
        this.securityManifest = securityManifest;
    }

    @Override
    public SecurityManifest getSecurityManifest() {
        loadAndRunIfNecessary();
        return securityManifest;
    }

    private void setStartupManifest(StartupManifest startupManifest) {
        this.startupManifest = startupManifest;
    }

    @Override
    public StartupManifest getStartupManifest() {
        loadAndRunIfNecessary();
        return startupManifest;
    }

    private void setEventManifest(EventManifest eventManifest) {
        this.eventManifest = eventManifest;
    }

    @Override
    public EventManifest getEventManifest() {
        loadAndRunIfNecessary();
        return eventManifest;
    }

    @Override
    public void loadAndRunIfNecessary() {
        if (!closed) {
            loadAndRun();
        }
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

    private void loadAndRun() {
        synchronized (lock) {
            if (closed) {
                throw new IllegalStateException("already closed");
            } else if (luaState != null) {
                return;
            }

            try (final InputStream inputStream = getAssetLoader().open(MAIN_MANIFEST)) {

                luaState = getLuaStateProvider().get();
                luaState.openLibs();

                builtinManager.installBuiltin(getClasspathBuiltinProvider().get());
                builtinManager.installBuiltin(getAssetLoaderBuiltinProvider().get());
                builtinManager.installBuiltin(new JavaObjectBuiltin<>(ATTRIBUTES_MODULE, emptyAttributes()));
                builtinManager.installBuiltin(new LoggerDetailBuiltin(() -> logger));

                setupFunctionOverrides();

                luaState.load(inputStream, MAIN_MANIFEST, "bt");
                scriptLogger.debug("Loaded Script: {}", MAIN_MANIFEST);

                luaState.call(0, LuaState.MULTRET);

                if (luaState.isTable(1)) {
                    luaState.getField(1, HTTP_TABLE);
                    HttpManifest httpManifest = luaState.toJavaObject(-1, HttpManifest.class);
                    if (httpManifest != null) {
                        scriptLogger.debug("Loaded Http Manifest");
                    }
                    this.setHttpManifest(httpManifest);

                    luaState.getField(1, MODEL_TABLE);
                    ModelManifest modelManifest = luaState.toJavaObject(-1, ModelManifest.class);
                    if (httpManifest != null) {
                        scriptLogger.debug("Loaded Model Manifest");
                    }
                    this.setModelManifest(modelManifest);

                    luaState.getField(1, SECURITY_TABLE);
                    SecurityManifest securityManifest = luaState.toJavaObject(-1, SecurityManifest.class);
                    if (httpManifest != null) {
                        scriptLogger.debug("Loaded Security Manifest");
                    }
                    this.setSecurityManifest(securityManifest);

                    luaState.getField(1, STARTUP_TABLE);
                    StartupManifest startupManifest = luaState.toJavaObject(-1, StartupManifest.class);
                    if (startupManifest != null) {
                        scriptLogger.debug("Loaded Startup Manifest");
                    }
                    this.setStartupManifest(startupManifest);

                    luaState.getField(1, EVENT_TABLE);
                    EventManifest eventManifest = luaState.toJavaObject(-1, EventManifest.class);
                    if (eventManifest != null) {
                        scriptLogger.debug("Loaded Event Manifest");
                    }
                    this.setEventManifest(eventManifest);
                }

                scriptLogger.debug("Finished Executing Script: {}", MAIN_MANIFEST);

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

    public Provider<AssetLoaderBuiltin> getAssetLoaderBuiltinProvider() {
        return assetLoaderBuiltinProvider;
    }

    @Inject
    public void setAssetLoaderBuiltinProvider(Provider<AssetLoaderBuiltin> assetLoaderBuiltinProvider) {
        this.assetLoaderBuiltinProvider = assetLoaderBuiltinProvider;
    }

    public Provider<ClasspathBuiltin> getClasspathBuiltinProvider() {
        return classpathBuiltinProvider;
    }

    @Inject
    public void setClasspathBuiltinProvider(Provider<ClasspathBuiltin> classpathBuiltinProvider) {
        this.classpathBuiltinProvider = classpathBuiltinProvider;
    }

}
