package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.dao.FileDao;
import dev.getelements.elements.dao.mongo.provider.MongoGridFSFileDaoProvider;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class MongoGridFSFileModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FileDao.class).toProvider(MongoGridFSFileDaoProvider.class);
    }

}
