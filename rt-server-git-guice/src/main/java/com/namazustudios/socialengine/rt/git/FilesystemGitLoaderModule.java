package com.namazustudios.socialengine.rt.git;

import com.google.inject.PrivateModule;

public class FilesystemGitLoaderModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(GitLoader.class);
        bind(GitLoader.class).to(FilesystemGitLoader.class).asEagerSingleton();
    }

}
