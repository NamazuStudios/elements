package com.namazustudios.socialengine.rest;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import redis.embedded.RedisServer;

import javax.inject.Inject;

public class EmbeddedRestApi {

    private RestAPIMain restAPIMain;

    private RedisServer redisServer;

    private MongodProcess mongodProcess;

    private MongodExecutable mongodExecutable;

    public void start() throws Exception {
        getRestAPIMain().start();
    }

    public void stop() throws Exception {
        getRestAPIMain().stop();
        getMongodExecutable().stop();
        getRedisServer().stop();
    }

    public RestAPIMain getRestAPIMain() {
        return restAPIMain;
    }

    @Inject
    public void setRestAPIMain(RestAPIMain restAPIMain) {
        this.restAPIMain = restAPIMain;
    }

    public RedisServer getRedisServer() {
        return redisServer;
    }

    @Inject
    public void setRedisServer(RedisServer redisServer) {
        this.redisServer = redisServer;
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
