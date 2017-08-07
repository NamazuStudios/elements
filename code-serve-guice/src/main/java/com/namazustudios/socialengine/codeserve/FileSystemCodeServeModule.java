package com.namazustudios.socialengine.codeserve;

import com.google.inject.AbstractModule;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class FileSystemCodeServeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApplicationRepositoryResolver.class).to(FileSystemApplicationRepositoryResolver.class);
    }

}
