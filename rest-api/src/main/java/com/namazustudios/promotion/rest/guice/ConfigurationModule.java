package com.namazustudios.promotion.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.namazustudios.promotion.Constants;
import com.namazustudios.promotion.dao.mongo.Atomic;
import com.namazustudios.promotion.dao.mongo.MongoShortLinkDao;
import com.namazustudios.promotion.dao.mongo.provider.MongoClientProvider;
import com.namazustudios.promotion.dao.mongo.provider.MongoDatastoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class ConfigurationModule extends AbstractModule {

    public static final String PROPERTIES_FILE = "com.namazustudios.promotions.configuration.properties";

    public static final String DEFAULT_PROPERTIES_FILE = "configuration.properties";

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationModule.class);

    @Override
    protected void configure() {

        final Properties defaultProperties = new Properties(System.getProperties());

        defaultProperties.setProperty(MongoClientProvider.MONGO_DB_URLS, "localhost");
        defaultProperties.setProperty(MongoDatastoreProvider.DATABASE_NAME, "promotions");
        defaultProperties.setProperty(Atomic.FALLOFF_TIME_MS, Integer.valueOf(100).toString());
        defaultProperties.setProperty(Atomic.OPTIMISTIC_RETRY_COUNT, Integer.valueOf(10).toString());
        defaultProperties.setProperty(MongoShortLinkDao.SHORT_LINK_BASE, "http://localhost/l/");
        defaultProperties.setProperty(Constants.QUERY_MAX_RESULTS, Integer.valueOf(20).toString());
        defaultProperties.setProperty(Constants.DIGEST_ALGORITHM, "SHA-256");
        defaultProperties.setProperty(Constants.PASSWORD_ENCODING, "UTF-8");

        final Properties properties = new Properties(defaultProperties);
        final File propertiesFile = new File(properties.getProperty(PROPERTIES_FILE, DEFAULT_PROPERTIES_FILE));

        try (final InputStream is = new FileInputStream(propertiesFile)) {
            properties.load(is);
        } catch (IOException ex) {
            LOG.warn("Could not load properties.", ex);
        }

        Names.bindProperties(binder(), properties);

    }

}
