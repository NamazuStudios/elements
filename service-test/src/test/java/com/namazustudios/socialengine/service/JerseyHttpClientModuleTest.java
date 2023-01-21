package com.namazustudios.socialengine.service;

import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.jersey.JerseyHttpClientModule;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@Guice(modules = JerseyHttpClientModule.class)
public class JerseyHttpClientModuleTest {

    private final JettyEmbeddedJSONService jettyEmbeddedJSONService = new JettyEmbeddedJSONService();

    @BeforeClass
    public void setup() throws Exception {
        jettyEmbeddedJSONService.start();
    }

    @AfterClass
    public void teardown() throws Exception {
        getClient().close();
        jettyEmbeddedJSONService.stop();
    }

    private Client client;

    @Test
    public void testGetCamelCase() {

        final Map<?,?> map = getClient().target(jettyEmbeddedJSONService.getUri())
            .path("lcamel")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .build("GET")
            .invoke(Map.class);

        final TestLCamelModel testModel = getClient().target(jettyEmbeddedJSONService.getUri())
            .path("lcamel")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .build("GET")
            .invoke(TestLCamelModel.class);

        assertEquals(map.size(), 1);
        assertEquals(map.get("testProperty"), "foo");
        Assert.assertEquals(testModel.getTestProperty(), "foo");

    }

    @Test
    public void testGetSnakeCase() {

        final Map<?,?> map = getClient().target(jettyEmbeddedJSONService.getUri())
            .path("snake")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .build("GET")
            .invoke(Map.class);

        final TestSnakeModel testModel = getClient().target(jettyEmbeddedJSONService.getUri())
            .path("snake")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .build("GET")
            .invoke(TestSnakeModel.class);

        assertEquals(map.size(), 1);
        assertEquals(map.get("test_property"), "foo");
        Assert.assertEquals(testModel.getTestProperty(), "foo");

    }

    @Test
    public void testGetDefaultCase() {

        final Map<?,?> map = getClient().target(jettyEmbeddedJSONService.getUri())
                .path("lcamel")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .build("GET")
                .invoke(Map.class);

        final TestDefaultModel testModel = getClient().target(jettyEmbeddedJSONService.getUri())
                .path("lcamel")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .build("GET")
                .invoke(TestDefaultModel.class);

        assertEquals(map.size(), 1);
        assertEquals(map.get("testProperty"), "foo");
        Assert.assertEquals(testModel.getTestProperty(), "foo");

    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
