package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
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

import static java.lang.String.format;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class MongoClientProvider implements Provider<MongoClient> {

    private static final Logger logger = LoggerFactory.getLogger(MongoClientProvider.class);

    public static final int DEFAULT_MONGO_PORT = 27017;

    public static final String MONGO_SCHEME = "mongo";

    public static final String MONGO_DB_URLS = "com.namazustudios.socialengine.mongo.db.url";

    public static final String MONGO_CLIENT_URI = "com.namazustudios.socialengine.mongo.client.uri";

    public static final String MONGO_MIN_CONNECTIONS = "com.namazustudios.socialengine.mongo.min.connections";

    public static final String MONGO_MAX_CONNECTIONS = "com.namazustudios.socialengine.mongo.max.connections";

    private int minConnections;

    private int maxConnections;

    private String mongoDbUrl;

    private String mongoDbUri;

    @Override
    public MongoClient get() {
        return !getMongoDbUrl().isEmpty() ? getWithLegacyOptions() :
               !getMongoDbUri().isEmpty() ? getWithClientUri()     :
               fail();
    }

    private MongoClient getWithLegacyOptions() {

        logger.info("Using legacy DB URL configuration.");

        final List<ServerAddress> serverAddressList = Arrays.asList(mongoDbUrl.split(",")).stream().map(input -> {

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

    private MongoClient getWithClientUri() {

        logger.info("Using MongoClient URI.");

        final MongoClientOptions.Builder mongoClientOptionsBuilder = MongoClientOptions.builder()
            .minConnectionsPerHost(getMinConnections())
            .connectionsPerHost(getMaxConnections());

        final MongoClientURI mongoClientURI = new MongoClientURI(getMongoDbUri(), mongoClientOptionsBuilder);

        return new MongoClient(mongoClientURI);

    }

    private MongoClient fail() {
        final String message = format("Must specify one of %s or %s", MONGO_DB_URLS, MONGO_CLIENT_URI);
        throw new IllegalStateException(message);
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

    public String getMongoDbUrl() {
        return mongoDbUrl;
    }

    @Inject
    public void setMongoDbUrl(@Named(MONGO_DB_URLS) String mongoDbUrl) {
        this.mongoDbUrl = mongoDbUrl;
    }

    public String getMongoDbUri() {
        return mongoDbUri;
    }

    @Inject
    public void setMongoDbUri(@Named(MONGO_CLIENT_URI) String mongoDbUri) {
        this.mongoDbUri = mongoDbUri;
    }

}
