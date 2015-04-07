package com.namazustudios.promotion.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;
import com.namazustudios.promotion.Constants;
import com.namazustudios.promotion.dao.PasswordDigestProvider;
import com.namazustudios.promotion.dao.SocialCampaignDao;
import com.namazustudios.promotion.dao.UserDao;
import com.namazustudios.promotion.dao.mongo.Atomic;
import com.namazustudios.promotion.dao.mongo.MongoShortLinkDao;
import com.namazustudios.promotion.dao.mongo.MongoSocialCampaignDao;
import com.namazustudios.promotion.dao.mongo.MongoUserDao;
import com.namazustudios.promotion.dao.mongo.provider.MongoClientProvider;
import com.namazustudios.promotion.dao.mongo.provider.MongoDatastoreProvider;
import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Properties;

/**
 * Configures any Mongo-specific system properties.
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

        defaultProperties.setProperty(MongoClientProvider.MONGO_DB_URLS, "localhost");
        defaultProperties.setProperty(MongoDatastoreProvider.DATABASE_NAME, "promotions");
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

        Names.bindProperties(binder(), properties);

        binder().bind(UserDao.class).to(MongoUserDao.class);
        binder().bind(SocialCampaignDao.class).to(MongoSocialCampaignDao.class);
        binder().bind(MongoShortLinkDao.class);
        binder().bind(MongoClient.class).toProvider(MongoClientProvider.class);
        binder().bind(Datastore.class).toProvider(MongoDatastoreProvider.class);

        binder().bind(MessageDigest.class)
                .annotatedWith(Names.named(Constants.PASSWORD_DIGEST))
                .toProvider(PasswordDigestProvider.class);

    }

}
