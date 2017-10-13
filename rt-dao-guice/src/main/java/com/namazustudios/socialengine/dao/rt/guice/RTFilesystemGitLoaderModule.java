package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.dao.rt.FilesystemGitLoader;
import com.namazustudios.socialengine.dao.rt.GitLoader;
import com.namazustudios.socialengine.dao.rt.provider.FileAssetLoaderProvider;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.AssetLoader;

import java.util.function.Function;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTFilesystemGitLoaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GitLoader.class).to(FilesystemGitLoader.class).asEagerSingleton();
        bind(new TypeLiteral<Function<Application, AssetLoader>>(){}).toProvider(FileAssetLoaderProvider.class);
    }

}
