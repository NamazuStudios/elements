package com.namazustudios.socialengine.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.dao.FileDao;
import com.namazustudios.socialengine.dao.mongo.provider.MongoGridFSFileDaoProvider;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class MongoGridFSFileModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FileDao.class).toProvider(MongoGridFSFileDaoProvider.class);
    }

}
