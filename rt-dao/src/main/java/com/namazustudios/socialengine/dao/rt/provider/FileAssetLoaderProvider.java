package com.namazustudios.socialengine.dao.rt.provider;

import com.namazustudios.socialengine.util.ShutdownHooks;
import com.namazustudios.socialengine.dao.rt.GitLoader;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.FileAssetLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Created by patricktwohig on 8/19/17.
 */
public class FileAssetLoaderProvider implements Provider<Function<Application, AssetLoader>> {

    private static final Logger logger = LoggerFactory.getLogger(FileAssetLoaderProvider.class);

    private static final ShutdownHooks hooks = new ShutdownHooks(FileAssetLoaderProvider.class);

    private Provider<GitLoader> gitLoaderProvider;

    private final ConcurrentMap<File, AssetLoader> loaderCache = new ConcurrentHashMap<>();

    public FileAssetLoaderProvider() {
        logger.info("Using File AssetLoader provider.");
    }

    @Override
    public Function<Application, AssetLoader> get() {
        return application -> {
            final File codeDirectory = getGitLoaderProvider().get().getCodeDirectory(application);
            return loaderCache.computeIfAbsent(codeDirectory, this::computeAssetLoader).getReferenceCountedView();
        };
    }

    private AssetLoader computeAssetLoader(final File file) {
        final AssetLoader assetLoader = new FileAssetLoader(file).getReferenceCountedView();
        hooks.add(assetLoader, assetLoader::close);
        return assetLoader;
    }

    public Provider<GitLoader> getGitLoaderProvider() {
        return gitLoaderProvider;
    }

    @Inject
    public void setGitLoaderProvider(Provider<GitLoader> gitLoaderProvider) {
        this.gitLoaderProvider = gitLoaderProvider;
    }

}
