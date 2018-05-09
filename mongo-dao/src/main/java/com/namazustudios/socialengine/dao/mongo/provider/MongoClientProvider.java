package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class MongoClientProvider implements Provider<MongoClient> {

    private static final Logger logger = LoggerFactory.getLogger(MongoClientProvider.class);

    public static final int DEFAULT_MONGO_PORT = 27017;

    public static final String MONGO_SCHEME = "mongo";

    public static final String MONGO_DB_URLS = "com.namazustudios.socialengine.mongo.db.url";

    public static final String MONGO_MIN_CONNECTIONS = "com.namazustudios.socialengine.mongo.min.connections";

    public static final String MONGO_MAX_CONNECTIONS = "com.namazustudios.socialengine.mongo.max.connections";

    private int minConnections;

    private int maxConnections;

    private String mongoDbUrls;

    @Override
    public MongoClient get() {

        final List<ServerAddress> serverAddressList = Arrays.asList(mongoDbUrls.split(",")).stream().map(input -> {

            final URI uri;

            try {
                uri = new URI(input);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Invalid URI", ex);
            }

            if (uri.getScheme() != null && !MONGO_SCHEME.equals(uri.getScheme())) {
                throw new IllegalArgumentException("Invalid scheme" + uri.getScheme());
            }

            final String host = uri.getHost();
            final int port = uri.getPort() < 0 ? DEFAULT_MONGO_PORT : uri.getPort();

            logger.info("Adding {}:{} to Mongo address list ({}).", host, port, uri);

            return new ServerAddress(host, port);

        }).collect(Collectors.toList());

        final MongoClientOptions mongoClientOptions = MongoClientOptions.builder()
            .minConnectionsPerHost(getMinConnections())
            .connectionsPerHost(getMaxConnections())
            .build();

        return new MongoClient(serverAddressList, mongoClientOptions);

    }

    public int getMinConnections() {
        return minConnections;
    }

    @Inject
    public void setMinConnections(@Named(MONGO_MIN_CONNECTIONS) int minConnections) {
        this.minConnections = minConnections;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    @Inject
    public void setMaxConnections(@Named(MONGO_MAX_CONNECTIONS) int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getMongoDbUrls() {
        return mongoDbUrls;
    }

    @Inject
    public void setMongoDbUrls(@Named(MONGO_DB_URLS) String mongoDbUrls) {
        this.mongoDbUrls = mongoDbUrls;
    }


}
