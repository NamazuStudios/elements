package com.namazustudios.socialengine.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.mongo.Atomic;
import com.namazustudios.socialengine.dao.mongo.provider.*;
import com.namazustudios.socialengine.fts.ObjectIndex;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Sets up and configures the {@link ObjectIndex} using the Mongo Lucene drivers
 * so that the search index can be stored directly in the mongo database.  This
 * includes some configuration parameters as well.
 *
 * Created by patricktwohig on 5/17/15.
 */
public class MongoSearchModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(MongoSearchModule.class);

//    public static final String SEARCH_INDEX_COLLECTION = "com.namazustudios.socialengine.mongo.search.index.collection";
//
//    public static final String DIRECTORY_KEY_NAME = "com.namazustudios.socialengine.mongo.search.index.directory.key.name";
//
//    public static final String DIRECTORY_ENTRY_NAME = "com.namazustudios.socialengine.mongo.search.index.directory.entry.name";


    @Override
    protected void configure() {

        final Properties defaultProperties = new Properties(System.getProperties());

        defaultProperties.setProperty(MongoDirectoryProvider.DIRECTORY_KEY_NAME, "key");
        defaultProperties.setProperty(MongoDirectoryProvider.DIRECTORY_ENTRY_NAME, "entry");
        defaultProperties.setProperty(MongoDirectoryProvider.SEARCH_INDEX_COLLECTION, "fts-index");

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

        binder().bind(Analyzer.class)
                .toProvider(MongoStandardAnalyzerProvider.class);

        binder().bind(ObjectIndex.class)
                .toProvider(MongoObjectIndexProvider.class)
                .in(Singleton.class);

    }

}
