//package com.namazustudios.socialengine.rt.lua;
//
//import com.google.inject.Inject;
//import com.namazustudios.socialengine.rt.exception.InternalException;
//import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
//
//import javax.inject.Provider;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//
///**
// * Used to create instances of {@link LuaHandler} from the IoC container.
// *
// * Created by patricktwohig on 9/2/15.
// */
//public class EdgeResourceProviders {
//
//    /**
//     * Returns a {@link Provider} which will return an instance of {@link LuaHandler} for
//     * the classpath entry.
//     *
//     * @param classpathLocation the classpath location
//     * @return the Provider instance
//     * @throws ResourceNotFoundException if the script can't be found at that location
//     */
//    public static Provider<LuaHandler> classpathProviderForScript(final String classpathLocation) {
//
//        final ClassLoader classLoader = EdgeResourceProviders.class.getClassLoader();
//        final URL resoureURL = classLoader.getResource(classpathLocation);
//
//        if (resoureURL == null) {
//            throw new ResourceNotFoundException("Resource at location does not exist: " + classpathLocation);
//        }
//
//        return new Provider<LuaHandler>() {
//
//            @Inject
//            private Provider<LuaHandler> luaEdgeResourceProvider;
//
//            @Override
//            public LuaHandler get() {
//
//                final LuaHandler luaEdgeResource = luaEdgeResourceProvider.get();
//
//                try (final InputStream inputStream = resoureURL.openStream()){
//                    final String simplifiedFileName = AbstractLuaResource.simlifyFileName(resoureURL.getFile());
//                    luaEdgeResource.loadAndRun(inputStream, simplifiedFileName);
//                } catch (IOException ex) {
//                    luaEdgeResource.close();
//                    throw new InternalException(ex);
//                }
//
//                return luaEdgeResource;
//
//            }
//
//        };
//
//    }
//
//    /**
//     * Returns a {@link Provider} which will return an instance of {@link LuaHandler} for
//     * the given {@link File}.
//     *
//     * @param file the file location
//     * @return the Provider instance
//     * @throws ResourceNotFoundException if the script can't be found at that location
//     */
//    public static Provider<LuaHandler> filesystemProviderForScript(final File file) {
//
//        try (final InputStream is = new FileInputStream(file)) {
//            // This just opens to check the file.  No actual reading
//            // of the file needs to happen.
//        } catch (IOException ex) {
//            throw new ResourceNotFoundException(ex);
//        }
//
//        return new Provider<LuaHandler>() {
//
//            @Inject
//            private Provider<LuaHandler> luaEdgeResourceProvider;
//
//            @Override
//            public LuaHandler get() {
//
//                final LuaHandler luaEdgeResource = luaEdgeResourceProvider.get();
//
//                try (final InputStream inputStream = new FileInputStream(file)) {
//                    final String simplifiedFileName = AbstractLuaResource.simlifyFileName(file.getAbsolutePath());
//                    luaEdgeResource.loadAndRun(inputStream, simplifiedFileName);
//                } catch (IOException ex) {
//                    luaEdgeResource.close();
//                    throw new ResourceNotFoundException(ex);
//                }
//
//                return luaEdgeResource;
//
//            }
//
//        };
//    }
//
//}
