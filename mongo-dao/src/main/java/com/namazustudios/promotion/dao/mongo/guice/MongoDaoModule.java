package com.namazustudios.promotion.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.mongodb.MongoClient;
import com.namazustudios.promotion.dao.SocialCampaignDao;
import com.namazustudios.promotion.dao.UserDao;
import com.namazustudios.promotion.dao.mongo.MongoShortLinkDao;
import com.namazustudios.promotion.dao.mongo.MongoSocialCampaignDao;
import com.namazustudios.promotion.dao.mongo.MongoUserDao;
import com.namazustudios.promotion.dao.mongo.model.MongoUser;
import com.namazustudios.promotion.dao.mongo.provider.MongoClientProvider;
import com.namazustudios.promotion.dao.mongo.provider.MongoDatastoreProvider;
import org.mongodb.morphia.Datastore;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class MongoDaoModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().bind(UserDao.class).to(MongoUserDao.class);
        binder().bind(SocialCampaignDao.class).to(MongoSocialCampaignDao.class);
        binder().bind(MongoShortLinkDao.class);
        binder().bind(MongoClient.class).toProvider(MongoClientProvider.class);
        binder().bind(Datastore.class).toProvider(MongoDatastoreProvider.class);
    }

}
