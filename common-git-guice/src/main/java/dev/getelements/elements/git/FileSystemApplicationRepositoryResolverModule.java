package dev.getelements.elements.git;

import com.google.inject.PrivateModule;
import dev.getelements.elements.git.ApplicationRepositoryResolver;
import dev.getelements.elements.git.FileSystemApplicationRepositoryResolver;

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
