package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;

import java.io.IOException;

import static de.flapdoodle.embed.mongo.MongodStarter.getDefaultInstance;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;

public class EmbeddedMongo implements AutoCloseable {

    public static final int TEST_MONGO_PORT = 45000;

    public static final String TEST_BIND_IP = "localhost";

    public static final String TEST_DATABASE = "lucene-test";

    private final MongodExecutable mongodExecutable;

    private final MongodProcess mongodProcess;

    private final MongoClient mongoClient;

    private final MongoDatabase mongoDatabase;

    public EmbeddedMongo() throws IOException {

        final IMongodConfig config = new MongodConfigBuilder()
            .version(Version.V3_4_1)
            .net(new Net(TEST_BIND_IP, TEST_MONGO_PORT, localhostIsIPv6()))
            .build();

        final MongodStarter starter = getDefaultInstance();
        this.mongodExecutable = starter.prepare(config);
        this.mongodProcess = mongodExecutable.start();

        final ServerAddress testServerAddress = new ServerAddress(TEST_BIND_IP, TEST_MONGO_PORT);
        this.mongoClient = new MongoClient(testServerAddress);
        this.mongoDatabase = this.mongoClient.getDatabase(TEST_DATABASE);
    }

    @Override
    public void close() throws IOException {
        this.mongoClient.close();
        this.mongodProcess.stop();
        this.mongodExecutable.stop();
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

}
