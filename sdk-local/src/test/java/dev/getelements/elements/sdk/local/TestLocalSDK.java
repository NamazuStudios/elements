package dev.getelements.elements.sdk.local;

import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.test.DockerMongoTestInstance;
import dev.getelements.elements.dao.mongo.test.MongoTestInstance;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.application.Application;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.Properties;

import static dev.getelements.elements.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static dev.getelements.elements.sdk.model.Constants.HTTP_PORT;
import static java.lang.String.format;

public class TestLocalSDK {

    private Application application;

    private ElementsLocal elementsLocal;

    private MongoTestInstance mongoTestInstance;

    private static final int TEST_MONGO_PORT = 47000;

    @BeforeClass
    public void setupMongoDb() {
        mongoTestInstance = new DockerMongoTestInstance(47000);
        mongoTestInstance.start();
    }

    @BeforeClass(dependsOnMethods = "setupMongoDb")
    public void setUpLocalRunner() {

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
                .withElementFromPacakge("MYAPP", "dev.getelements.elements.sdk.test.element.rs")
                .withElementFromPacakge("MYAPP", "dev.getelements.elements.sdk.test.element.ws")
                .build();

    }

    @BeforeClass(dependsOnMethods = "setUpLocalRunner")
    public void setupApplication() {

        final var dao = elementsLocal
                .getRootElementRegistry()
                .find("dev.getelements.elements.sdk.dao")
                .findFirst()
                .get();

        final var applicationDao = dao
                .getServiceLocator()
                .getInstance(ApplicationDao.class);

        final var application = new Application();
        application.setName("MYAPP");
        application.setDescription("My Application");

        this.application = applicationDao.createOrUpdateInactiveApplication(application);

    }

    @BeforeClass(dependsOnMethods = "setupApplication")
    public void startInstance() {
        elementsLocal.start();
    }

    @AfterClass
    public void tearDownMongoDb() {
        mongoTestInstance.stop();
    }

}
