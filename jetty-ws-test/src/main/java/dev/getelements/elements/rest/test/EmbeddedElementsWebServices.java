package dev.getelements.elements.rest.test;

import dev.getelements.elements.dao.mongo.test.MongoTestInstance;
import dev.getelements.elements.jetty.ElementsWebServices;
import dev.getelements.elements.test.EmbeddedTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.embedded.RedisServer;

import javax.inject.Inject;

import static java.lang.Runtime.getRuntime;

public class EmbeddedElementsWebServices {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedElementsWebServices.class);

    private final ElementsWebServices elementsWebServices;

    private final RedisServer redisServer;

    private final MongoTestInstance mongoTestInstance;

    private final EmbeddedTestService embeddedTestService;

    @Inject
    public EmbeddedElementsWebServices(final ElementsWebServices elementsWebService,
                                       final RedisServer redisServer,
                                       final MongoTestInstance mongoTestInstance,
                                       final EmbeddedTestService embeddedTestService) throws Exception {

        this.elementsWebServices = elementsWebService;
        this.redisServer = redisServer;
        this.mongoTestInstance = mongoTestInstance;
        this.embeddedTestService = embeddedTestService;

        getElementsWebServices().start();

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
            getElementsWebServices().stop();
        } catch (Exception ex) {
            logger.warn("Caught exception stopping API.  Disregarding.", ex);
        }

        try {
            getMongoTestInstance().stop();
        } catch (Exception ex) {
            logger.warn("Caught exception MongoDB.  Disregarding.", ex);
        }

        try {
            getRedisServer().stop();
        } catch (Exception ex) {
            logger.warn("Caught exception Redis.  Disregarding.", ex);
        }

        try {
            getEmbeddedTestService().close();
        } catch (Exception ex) {
            logger.warn("Caught exception closing embedded test service.", ex);
        }

    }

    public ElementsWebServices getElementsWebServices() {
        return elementsWebServices;
    }

    public RedisServer getRedisServer() {
        return redisServer;
    }

    public MongoTestInstance getMongoTestInstance() {
        return mongoTestInstance;
    }

    public EmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

}
