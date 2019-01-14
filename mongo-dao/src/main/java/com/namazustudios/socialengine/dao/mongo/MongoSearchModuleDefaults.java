package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.elements.fts.concurrent.jeromq.DynamicJeroMQBindStrategy;
import com.namazustudios.elements.fts.concurrent.jeromq.DynamicJeroMQBindStrategyBuilder;
import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.dao.mongo.provider.JeroMQConditionProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDirectoryProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoLockFactoryProvider;

import java.util.Properties;

import static com.namazustudios.elements.fts.concurrent.jeromq.DynamicJeroMQBindStrategy.DEFAULT_PORT_RANGE;
import static com.namazustudios.elements.fts.concurrent.jeromq.DynamicJeroMQBindStrategyBuilder.DEFAULT_BIND_ADDRESS;
import static com.namazustudios.elements.fts.concurrent.jeromq.DynamicJeroMQBindStrategyBuilder.DEFAULT_TRANSPORT;
import static java.lang.String.format;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoSearchModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties(System.getProperties());
        defaultProperties.setProperty(MongoLockFactoryProvider.PEER_COLLECTION, "fts.peers");
        defaultProperties.setProperty(MongoLockFactoryProvider.LOCK_COLLECTION, "fts.locks");
        defaultProperties.setProperty(MongoDirectoryProvider.SEARCH_INDEX_BUCKET, "fts.index");
        defaultProperties.setProperty(JeroMQConditionProvider.BIND_ADDRESS, DEFAULT_BIND_ADDRESS);
        defaultProperties.setProperty(JeroMQConditionProvider.HOST_ADDRESS, format("%s://127.0.0.1", DEFAULT_TRANSPORT));
        return defaultProperties;
    }

}
