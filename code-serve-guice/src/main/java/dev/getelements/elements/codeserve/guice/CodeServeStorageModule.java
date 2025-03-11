package dev.getelements.elements.codeserve.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.git.ApplicationRepositoryResolver;
import dev.getelements.elements.git.FileSystemApplicationRepositoryResolver;

public class CodeServeStorageModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApplicationRepositoryResolver.class)
                .to(FileSystemApplicationRepositoryResolver.class)
                .asEagerSingleton();
    }

}
