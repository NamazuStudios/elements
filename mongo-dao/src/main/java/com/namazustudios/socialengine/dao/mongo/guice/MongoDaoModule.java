package com.namazustudios.socialengine.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.dao.mongo.*;
import com.namazustudios.socialengine.dao.mongo.provider.MongoAdvancedDatastoreProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDatastoreProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDozerMapperProvider;
import com.namazustudios.socialengine.fts.ObjectIndex;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;

import javax.inject.Singleton;
import java.security.MessageDigest;

import static com.google.inject.util.Providers.guicify;

/**
 * Configures any Mongo-specific system properties.
 *
 * This is intentionally separated from the {@link MongoSearchModule} as it may or
 * may not be desirable to use the mongo search, depending on the application.
 *
 * The only dependency this module has but does not not provide is one of
 * an instance of {@link ObjectIndex}.
 *
 * Created by patricktwohig on 4/3/15.
 */
public class MongoDaoModule extends AbstractModule {

    @Override
    protected void configure() {

        binder().bind(UserDao.class).to(MongoUserDao.class);
        binder().bind(SocialCampaignDao.class).to(MongoSocialCampaignDao.class);
        binder().bind(ShortLinkDao.class).to(MongoShortLinkDao.class);
        binder().bind(ApplicationDao.class).to(MongoApplicationDao.class);
        binder().bind(ApplicationConfigurationDao.class).to(MongoApplicationConfigurationDao.class);
        binder().bind(IosApplicationConfigurationDao.class).to(MongoIosApplicationConfigurationDao.class);
        binder().bind(GooglePlayApplicationConfigurationDao.class).to(MongoGoogePlayApplicationConfigurationDao.class);
        binder().bind(FacebookApplicationConfigurationDao.class).to(MongoFacebookApplicationConfigurationDao.class);

        binder().bind(MongoClient.class).toProvider(MongoClientProvider.class).in(Singleton.class);
        binder().bind(Datastore.class).toProvider(MongoDatastoreProvider.class);
        binder().bind(AdvancedDatastore.class).toProvider(MongoAdvancedDatastoreProvider.class);

        binder().bind(MessageDigest.class)
                .annotatedWith(Names.named(Constants.PASSWORD_DIGEST))
                .toProvider(PasswordDigestProvider.class);

        binder().bind(Mapper.class).toProvider(guicify(new MongoDozerMapperProvider()));

    }

}
