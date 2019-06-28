package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rest.guice.model.TestDefaultModel;
import com.namazustudios.socialengine.rest.guice.model.TestLCamelModel;
import com.namazustudios.socialengine.rest.guice.model.TestSnakeModel;
import com.namazustudios.socialengine.rest.guice.servlet.JettyEmbeddedJSONService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import java.util.Map;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.google.inject.Guice.createInjector;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.SNAKE;
import static org.testng.Assert.assertEquals;

@Guice(modules = JacksonHttpClientModuleTest.Module.class)
public class JacksonHttpClientModuleTest {

    private final JettyEmbeddedJSONService jettyEmbeddedJSONService = new JettyEmbeddedJSONService();

    @BeforeClass
    public void setup() throws Exception {
        jettyEmbeddedJSONService.start();
    }

    @AfterClass
    public void teardown() throws Exception {
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
        assertEquals(testModel.getTestProperty(), "foo");

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
        assertEquals(testModel.getTestProperty(), "foo");

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
        assertEquals(testModel.getTestProperty(), "foo");

    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public static class Module extends AbstractModule {
        @Override
        protected void configure() {
            install(new JacksonHttpClientModule().withDefaultObjectMapperProvider(()-> {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
                return objectMapper;
            }).withNamedObjectMapperProvider(SNAKE, () -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            }));
        }
    }

}
