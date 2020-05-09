package com.namazustudios.socialengine.rest;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.embedded.RedisServer;

import javax.inject.Inject;

import java.util.concurrent.Callable;

import static java.lang.Runtime.getRuntime;

public class EmbeddedRestApi {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedRestApi.class);

    private final RestAPIMain restAPIMain;

    private final RedisServer redisServer;

    private final MongodProcess mongodProcess;

    private final MongodExecutable mongodExecutable;

    @Inject
    public EmbeddedRestApi(final RestAPIMain restAPIMain,
                           final RedisServer redisServer,
                           final MongodProcess mongodProcess,
                           final MongodExecutable mongodExecutable) throws Exception {
        this.restAPIMain = restAPIMain;
        this.redisServer = redisServer;
        this.mongodProcess = mongodProcess;
        this.mongodExecutable = mongodExecutable;
        getRestAPIMain().start();
        getRuntime().addShutdownHook(new Thread(() ->{
            try {
                stop();
            } catch (Exception ex) {
                logger.error("Could not stop services.", ex);
            }
        }));
    }

    public void stop() {

        try {
            getRestAPIMain().stop();
        } catch (Exception ex) {
            logger.warn("Caught exception stopping API.  Disregarding.", ex);
        }

        try {
            getMongodExecutable().stop();
        } catch (Exception ex) {
            logger.warn("Caught exception MongoDB.  Disregarding.", ex);
        }

        try {
            getRedisServer().stop();
        } catch (Exception ex) {
            logger.warn("Caught exception Redis.  Disregarding.", ex);
        }

    }

    public RestAPIMain getRestAPIMain() {
        return restAPIMain;
    }

    public RedisServer getRedisServer() {
        return redisServer;
    }

    public MongodProcess getMongodProcess() {
        return mongodProcess;
    }

    public MongodExecutable getMongodExecutable() {
        return mongodExecutable;
    }
}
