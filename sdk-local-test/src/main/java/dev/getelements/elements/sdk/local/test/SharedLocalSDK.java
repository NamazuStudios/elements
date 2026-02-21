package dev.getelements.elements.sdk.local.test;

import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.sdk.local.ElementsLocal;
import dev.getelements.elements.sdk.local.ElementsLocalBuilder;
import dev.getelements.elements.sdk.mongo.test.DockerMongoTestInstance;
import dev.getelements.elements.sdk.mongo.test.MongoTestInstance;
import dev.getelements.elements.sdk.util.ShutdownHooks;

import java.util.Properties;

import static dev.getelements.elements.sdk.model.Constants.HTTP_PORT;
import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;
import static java.lang.String.format;

public class SharedLocalSDK {

    private static final int TEST_MONGO_PORT = 47000;

    private static final SharedLocalSDK instance = new SharedLocalSDK();

    /**
     * Gets the shared instance.
     * @return
     */
    public static SharedLocalSDK getInstance() {
        return instance;
    }

    private final ElementsLocal elementsLocal;

    private final MongoTestInstance mongoTestInstance;

    private final ShutdownHooks shutdownHooks = new ShutdownHooks(TestLocalSDKElementPath.class);

    private SharedLocalSDK() {

        mongoTestInstance = new DockerMongoTestInstance(47000);
        mongoTestInstance.start();
        shutdownHooks.add(mongoTestInstance::stop);

        final var configurationSupplier = new DefaultConfigurationSupplier() {
            @Override
            public Properties get() {
                final var properties = super.get();
                properties.put(HTTP_PORT, "8181");
                properties.put(MONGO_CLIENT_URI, format("mongodb://127.0.0.1:%d", TEST_MONGO_PORT));
                return properties;
            }
        };

        elementsLocal = ElementsLocalBuilder.getDefault()
                .withProperties(configurationSupplier.get())
                .build();

        elementsLocal.start();
        shutdownHooks.add(elementsLocal::close);

    }

    public ElementsLocal getElementsLocal() {
        return elementsLocal;
    }

    public MongoTestInstance getMongoTestInstance() {
        return mongoTestInstance;
    }

}
