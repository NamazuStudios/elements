package com.namazustudios.socialengine.rt.lua;

import com.google.inject.Inject;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;

import javax.inject.Provider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by patricktwohig on 9/2/15.
 */
public class InternalResourceProviders {

    /**
     * Returns a {@link Provider} which will return an instance of {@link LuaInternalResource} for
     * the classpath entry.
     *
     * @param classpathLocation the classpath location
     * @return the Provider instance
     * @throws NotFoundException if the script can't be found at that location
     */
    public static Provider<LuaInternalResource> classpathProviderForScript(final String classpathLocation) {

        final ClassLoader classLoader = EdgeResourceProviders.class.getClassLoader();
        final URL resoureURL = classLoader.getResource(classpathLocation);

        if (resoureURL == null) {
            throw new NotFoundException("Resource at location does not exist: " + classpathLocation);
        }

        return new Provider<LuaInternalResource>() {

            @Inject
            private Provider<LuaInternalResource> luaInternalResourceProvider;

            @Override
            public LuaInternalResource get() {

                final LuaInternalResource luaInternalResource = luaInternalResourceProvider.get();

                try (final InputStream inputStream = resoureURL.openStream()){
                    luaInternalResource.loadAndRun(inputStream, resoureURL.toString());
                } catch (IOException ex) {
                    throw new InternalException(ex);
                }

                return luaInternalResource;

            }

        };

    }

    /**
     * Returns a {@link Provider} which will return an instance of {@link LuaInternalResource} for
     * the given {@link File}.
     *
     * @param file the file location
     * @return the Provider instance
     * @throws NotFoundException if the script can't be found at that location
     */
    public static Provider<LuaInternalResource> filesystemProviderForScript(final File file) {

        try (final InputStream is = new FileInputStream(file)) {
            // This just opens to check the file.  No actual reading
            // of the file needs to happen.
        } catch (IOException ex) {
            throw new NotFoundException(ex);
        }

        return new Provider<LuaInternalResource>() {

            @Inject
            private Provider<LuaInternalResource> luaInternalResourceProvider;

            @Override
            public LuaInternalResource get() {

                final LuaInternalResource luaEdgeResource = luaInternalResourceProvider.get();

                try (final InputStream inputStream = new FileInputStream(file)) {
                    luaEdgeResource.loadAndRun(inputStream, file.getAbsolutePath());
                } catch (IOException ex) {
                    throw new NotFoundException(ex);
                }

                return luaEdgeResource;

            }

        };
    }
}
