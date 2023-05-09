package dev.getelements.elements.codeserve;

import com.google.inject.PrivateModule;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class FileSystemApplicationRepositoryResolverModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(ApplicationRepositoryResolver.class);
        bind(ApplicationRepositoryResolver.class).to(FileSystemApplicationRepositoryResolver.class);
    }

}
