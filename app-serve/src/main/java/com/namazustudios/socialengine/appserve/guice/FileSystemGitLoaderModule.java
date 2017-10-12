package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.dao.rt.FilesystemGitLoader;
import com.namazustudios.socialengine.dao.rt.GitLoader;

public class FileSystemGitLoaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GitLoader.class).to(FilesystemGitLoader.class).asEagerSingleton();
    }

}
