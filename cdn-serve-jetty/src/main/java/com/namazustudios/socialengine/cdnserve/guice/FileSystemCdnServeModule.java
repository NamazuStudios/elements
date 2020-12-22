package com.namazustudios.socialengine.cdnserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.codeserve.ApplicationRepositoryResolver;
import com.namazustudios.socialengine.codeserve.FileSystemApplicationRepositoryResolver;

/**
 * Created by garrettmcspadden on 12/21/20.
 */
public class FileSystemCdnServeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApplicationRepositoryResolver.class).to(FileSystemApplicationRepositoryResolver.class);
    }

}
