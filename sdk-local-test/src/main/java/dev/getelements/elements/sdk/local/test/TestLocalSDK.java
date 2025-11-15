package dev.getelements.elements.sdk.local.test;

import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.sdk.mongo.test.DockerMongoTestInstance;
import dev.getelements.elements.sdk.mongo.test.MongoTestInstance;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.local.ElementsLocal;
import dev.getelements.elements.sdk.local.ElementsLocalBuilder;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.util.ShutdownHooks;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Properties;

import static dev.getelements.elements.sdk.model.Constants.HTTP_PORT;
import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;
import static java.lang.String.format;

public class TestLocalSDK {

    private Application application;

    private ElementsLocal elementsLocal;

    private MongoTestInstance mongoTestInstance;

    private static final ShutdownHooks shutdownHooks = new ShutdownHooks(TestLocalSDK.class);

    private static final int TEST_MONGO_PORT = 47000;

    @BeforeClass
    public void setupMongoDb() throws InterruptedException {
        mongoTestInstance = new DockerMongoTestInstance(47000);
        mongoTestInstance.start();
        shutdownHooks.add(mongoTestInstance::stop);
        Thread.sleep(1000);
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
                .withElementNamed("MYAPP", "dev.getelements.elements.sdk.test.element.rs")
                .withElementNamed("MYAPP", "dev.getelements.elements.sdk.test.element.ws")
                .build();

        shutdownHooks.add(elementsLocal::close);

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
    public void tearDownInstance() {
        elementsLocal.close();
    }

    @AfterClass
    public void tearDownMongoDb() {
        mongoTestInstance.close();
    }

    @Test
    public void testCallVersionEndpoint() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client.target("http://localhost:8181/api/rest/version")
                    .request()
                    .get();

            final var status = response.getStatus();
            Assert.assertEquals(status, Response.Status.OK.getStatusCode());

        }
    }

    @Test
    public void testGetMessages() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client.target("http://localhost:8181/app/rest/myapp/message")
                    .request()
                    .get();

            final var status = response.getStatus();
            Assert.assertEquals(status, Response.Status.OK.getStatusCode());

        }
    }

}
