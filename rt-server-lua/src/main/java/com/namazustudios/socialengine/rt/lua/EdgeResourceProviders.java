package com.namazustudios.socialengine.rt.lua;

import com.google.inject.Inject;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;

import javax.inject.Provider;
import java.io.*;
import java.net.URL;

/**
 * Used to create instances of {@link LuaEdgeResource} from the IoC container.
 *
 * Created by patricktwohig on 9/2/15.
 */
public class EdgeResourceProviders {

    /**
     * Returns a {@link Provider} which will return an instance of {@link LuaEdgeResource} for
     * the classpath entry.
     *
     * @param classpathLocation the classpath location
     * @return the Provider instance
     * @throws NotFoundException if the script can't be found at that location
     */
    public static Provider<LuaEdgeResource> classpathProviderForScript(final String classpathLocation) {

        final ClassLoader classLoader = EdgeResourceProviders.class.getClassLoader();
        final URL resoureURL = classLoader.getResource(classpathLocation);

        if (resoureURL == null) {
            throw new NotFoundException("Resource at location does not exist: " + classpathLocation);
        }

        return new Provider<LuaEdgeResource>() {

            @Inject
            private Provider<LuaEdgeResource> luaEdgeResourceProvider;

            @Override
            public LuaEdgeResource get() {

                final LuaEdgeResource luaEdgeResource = luaEdgeResourceProvider.get();

                try (final InputStream inputStream = resoureURL.openStream()){
                    luaEdgeResource.load(inputStream, resoureURL.toString());
                } catch (IOException ex) {
                    throw new InternalException(ex);
                }

                return luaEdgeResource;

            }

        };

    }

    /**
     * Returns a {@link Provider} which will return an instance of {@link LuaEdgeResource} for
     * the given {@link File}.
     *
     * @param file the file location
     * @return the Provider instance
     * @throws NotFoundException if the script can't be found at that location
     */
    public static Provider<LuaEdgeResource> filesystemProviderForScript(final File file) {

        try (final InputStream is = new FileInputStream(file)) {
            // This just opens to check the file.  No actual reading
            // of the file needs to happen.
        } catch (IOException ex) {
            throw new NotFoundException(ex);
        }

        return new Provider<LuaEdgeResource>() {

            @Inject
            private Provider<LuaEdgeResource> luaEdgeResourceProvider;

            @Override
            public LuaEdgeResource get() {

                final LuaEdgeResource luaEdgeResource = luaEdgeResourceProvider.get();

                try (final InputStream inputStream = new FileInputStream(file)) {
                    luaEdgeResource.load(inputStream, file.getAbsolutePath());
                } catch (IOException ex) {
                    throw new NotFoundException(ex);
                }

                return luaEdgeResource;

            }

        };
    }

}
