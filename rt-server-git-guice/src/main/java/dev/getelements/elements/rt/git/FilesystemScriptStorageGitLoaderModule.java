package dev.getelements.elements.rt.git;

import com.google.inject.PrivateModule;

public class FilesystemScriptStorageGitLoaderModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(GitLoader.class);
        bind(GitLoader.class).to(FilesystemScriptStorageGitLoader.class).asEagerSingleton();
    }

}
