package dev.getelements.elements.cdnserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.cdnserve.FilesystemCdnApplicationRepositoryResolverProvider;
import dev.getelements.elements.git.ApplicationRepositoryResolver;

public class FileSystemCdnGitStorageModule extends PrivateModule {

    private static final TypeLiteral<ApplicationRepositoryResolver> RESOLVER_TYPE = new TypeLiteral<>(){};

    @Override
    protected void configure() {

        bind(ApplicationRepositoryResolver.class)
                .toProvider(FilesystemCdnApplicationRepositoryResolverProvider.class)
                .asEagerSingleton();

        expose(ApplicationRepositoryResolver.class);

    }

}
