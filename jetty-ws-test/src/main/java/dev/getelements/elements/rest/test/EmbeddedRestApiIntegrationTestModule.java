package dev.getelements.elements.rest.test;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.spi.ProvisionListener;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.test.MongoTestInstanceModule;
import dev.getelements.elements.deployment.jetty.guice.JettySdkElementModule;
import dev.getelements.elements.jetty.*;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.application.Application;

import java.util.Properties;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rest.test.ClientContext.CONTEXT_APPLICATION;
import static dev.getelements.elements.rest.test.TestUtils.*;
import static dev.getelements.elements.sdk.model.Constants.HTTP_PORT;
import static dev.getelements.elements.sdk.mongo.standard.StandardMongoConfigurationService.MONGO_CLIENT_URI;
import static java.lang.String.format;

public class EmbeddedRestApiIntegrationTestModule extends AbstractModule {

    private static final int TEST_MONGO_PORT = 46000;

    private static final String TEST_MONGO_BIND_IP = "127.0.0.1";

    @Override
    protected void configure() {

        final var configurationSupplier = new DefaultConfigurationSupplier() {
            @Override
            public Properties get() {
                final var properties = super.get();
                properties.put(HTTP_PORT, "8181");
                properties.put(TEST_API_ROOT, "http://localhost:8181/api/rest");
                properties.put(TEST_APP_SERVE_WS_ROOT, "ws://localhost:8181/app/ws");
                properties.put(TEST_APP_SERVE_RS_ROOT, "http://localhost:8181/app/rest");
                properties.put(MONGO_CLIENT_URI, format("mongodb://%s:%d", TEST_MONGO_BIND_IP, TEST_MONGO_PORT));
                return properties;
            }
        };

        install(new TestVersionServiceModule());
        install(new MongoTestInstanceModule(TEST_MONGO_PORT));
        install(new JettySdkElementModule());

        install(new JettyServerModule());
        install(new ElementsCoreModule(configurationSupplier));
        install(new ElementsWebServiceComponentModule());

        final var applicationDaoProvider = getProvider(ApplicationDao.class);

        bind(Application.class).annotatedWith(named(CONTEXT_APPLICATION)).toProvider(() -> {
            final var application = new Application();
            application.setName("CXTTAPP");
            application.setDescription("Context Test Application");
            final var applicationDao = applicationDaoProvider.get();
            return applicationDao.createOrUpdateInactiveApplication(application);
        }).asEagerSingleton();

        bind(ElementsWebServices.class).asEagerSingleton();

        bindListener(this::isElementsWebServices, new ProvisionListener() {
            @Override
            public <T> void onProvision(final ProvisionInvocation<T> provision) {
                final var ews = (ElementsWebServices) provision.provision();
                ews.start();
            }
        });

    }

    private boolean isElementsWebServices(final Binding<?> binding) {
        return ElementsWebServices.class.isAssignableFrom(binding.getKey().getTypeLiteral().getRawType());
    }

}
