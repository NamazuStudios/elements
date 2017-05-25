package com.namazustudios.socialengine.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.dao.mongo.*;
import com.namazustudios.socialengine.dao.mongo.provider.MongoAdvancedDatastoreProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDatabaseProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDatastoreProvider;
import com.namazustudios.socialengine.fts.ObjectIndex;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Properties;

import static com.google.inject.name.Names.bindProperties;

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

    private static final Logger LOG = LoggerFactory.getLogger(MongoDaoModule.class);

    public static final int DEFAULT_FALLOFF_TIME_MS = 100;

    public static final int OPTISMITIC_RETRY_COUNT = 10;

    @Override
    protected void configure() {

        final Properties defaultProperties = new Properties(System.getProperties());

        defaultProperties.setProperty(MongoClientProvider.MONGO_DB_URLS, "mongo://localhost");
        defaultProperties.setProperty(MongoDatabaseProvider.DATABASE_NAME, "socialengine");
        defaultProperties.setProperty(Atomic.FALLOFF_TIME_MS, Integer.toString(DEFAULT_FALLOFF_TIME_MS));
        defaultProperties.setProperty(Atomic.OPTIMISTIC_RETRY_COUNT, Integer.toString(OPTISMITIC_RETRY_COUNT));

        final Properties properties = new Properties(defaultProperties);
        final File propertiesFile = new File(properties.getProperty(
                Constants.PROPERTIES_FILE,
                Constants.DEFAULT_PROPERTIES_FILE));

        try (final InputStream is = new FileInputStream(propertiesFile)) {
            properties.load(is);
        } catch (IOException ex) {
            LOG.warn("Could not load properties.  Using defaults.", ex);
        }

        LOG.info("Using configuration properties " + properties);

        bindProperties(binder(), properties);

        binder().bind(UserDao.class).to(MongoUserDao.class);
        binder().bind(SocialCampaignDao.class).to(MongoSocialCampaignDao.class);
        binder().bind(ShortLinkDao.class).to(MongoShortLinkDao.class);
        binder().bind(ApplicationDao.class).to(MongoApplicationDao.class);
        binder().bind(ApplicationProfileDao.class).to(MongoApplicationProfileDao.class);
        binder().bind(IosApplicationProfileDao.class).to(MongoIosApplicationProfileDao.class);
        binder().bind(GooglePlayApplicationProfileDao.class).to(MongoGoogePlayApplicationProfileDao.class);

        binder().bind(MongoClient.class).toProvider(MongoClientProvider.class).in(Singleton.class);
        binder().bind(Datastore.class).toProvider(MongoDatastoreProvider.class);
        binder().bind(AdvancedDatastore.class).toProvider(MongoAdvancedDatastoreProvider.class);

        binder().bind(MessageDigest.class)
                .annotatedWith(Names.named(Constants.PASSWORD_DIGEST))
                .toProvider(PasswordDigestProvider.class);

    }

}
