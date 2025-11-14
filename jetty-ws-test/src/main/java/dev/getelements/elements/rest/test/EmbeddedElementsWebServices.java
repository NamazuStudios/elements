package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.mongo.test.MongoTestInstance;
import dev.getelements.elements.jetty.ElementsWebServices;
import dev.getelements.elements.sdk.util.ShutdownHooks;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedElementsWebServices {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedElementsWebServices.class);

    private static final ShutdownHooks shutdownHooks = new ShutdownHooks(EmbeddedElementsWebServices.class);

    private final MongoTestInstance mongoTestInstance;

    private final ElementsWebServices elementsWebServices;

    @Inject
    public EmbeddedElementsWebServices(final ElementsWebServices elementsWebService,
                                       final MongoTestInstance mongoTestInstance) {
        this.elementsWebServices = elementsWebService;
        this.mongoTestInstance = mongoTestInstance;
        shutdownHooks.add(this::stop);
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

    }

    public MongoTestInstance getMongoTestInstance() {
        return mongoTestInstance;
    }

    public ElementsWebServices getElementsWebServices() {
        return elementsWebServices;
    }

}
