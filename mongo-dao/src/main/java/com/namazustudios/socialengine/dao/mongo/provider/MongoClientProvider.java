package com.namazustudios.socialengine.dao.mongo.provider;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

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

    public static final int DEFAULT_MONGO_PORT = 27017;

    public static final String MONGO_SCHEME = "mongo";

    public static final String MONGO_DB_URLS = "com.namazustudios.socialengine.mongo.db.url";

    @Inject
    @Named(MONGO_DB_URLS)
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

            return new ServerAddress(host, port);

        }).collect(Collectors.toList());

        return new MongoClient(serverAddressList);

    }

}
