//package com.namazustudios.socialengine.dao.rt.guice;
//
//import com.google.inject.Injector;
//import com.namazustudios.socialengine.dao.rt.GitLoader;
//import com.namazustudios.socialengine.model.application.Application;
//import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
//import com.namazustudios.socialengine.rt.guice.FileAssetLoaderModule;
//import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//import java.io.File;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.function.Function;
//
//public class RTGitApplicationInjectorProvider implements Provider<Function<Application, Injector>> {
//
//    private Injector parentInjector;
//
//    private Provider<GitLoader> gitLoaderProvider;
//
//    private final ConcurrentMap<String, Injector> applicationIdInjectorMap = new ConcurrentHashMap<>();
//
//    @Override
//    public Function<Application, Injector> get() {
//        return application -> applicationIdInjectorMap.computeIfAbsent(application.getId(), applicationId -> {
//
//            final File codeDirectory = getGitLoaderProvider().get().getCodeDirectory(application);
//
//            return getParentInjector().createChildInjector(
//                    new LuaModule() {
//                        @Override
//                        protected void configureFeatures() {
//                            // TODO We will eventually need to enable resource loader support.
//                            enableBasicConverters();
//                            enableManifestLoaderFeature();
//                        }
//                    },
//                    new GuiceIoCResolverModule(),
//                    new FileAssetLoaderModule(codeDirectory)
//            );
//
//        });
//    }
//
//    public Injector getParentInjector() {
//        return parentInjector;
//    }
//
//    @Inject
//    public void setParentInjector(Injector parentInjector) {
//        this.parentInjector = parentInjector;
//    }
//
//    public Provider<GitLoader> getGitLoaderProvider() {
//        return gitLoaderProvider;
//    }
//
//    @Inject
//    public void setGitLoaderProvider(Provider<GitLoader> gitLoaderProvider) {
//        this.gitLoaderProvider = gitLoaderProvider;
//    }
//
//}
