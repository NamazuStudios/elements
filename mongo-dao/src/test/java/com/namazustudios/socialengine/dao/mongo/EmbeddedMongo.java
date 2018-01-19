package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;

import javax.inject.Inject;

public class EmbeddedMongo {

    private MongoDatabase mongoDatabase;

    private MongodProcess mongodProcess;

    private MongodExecutable mongodExecutable;

    public void stop() {
        getMongodProcess().stop();
        getMongodExecutable().stop();
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    @Inject
    public void setMongoDatabase(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public MongodProcess getMongodProcess() {
        return mongodProcess;
    }

    @Inject
    public void setMongodProcess(MongodProcess mongodProcess) {
        this.mongodProcess = mongodProcess;
    }

    public MongodExecutable getMongodExecutable() {
        return mongodExecutable;
    }

    @Inject
    public void setMongodExecutable(MongodExecutable mongodExecutable) {
        this.mongodExecutable = mongodExecutable;
    }

}
