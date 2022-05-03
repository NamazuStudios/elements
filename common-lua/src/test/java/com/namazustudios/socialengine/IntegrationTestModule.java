package com.namazustudios.socialengine;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.rt.guice.ClasspathAssetLoaderModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.service.NotificationBuilder;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import com.namazustudios.socialengine.test.JeroMQEmbeddedTestService;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.spy;

public class IntegrationTestModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestModule.class);

    private final JeroMQEmbeddedTestService embeddedTestService;

    private static final AtomicInteger testPort = new AtomicInteger(45000);

    private static final String TEST_BIND_IP = "localhost";

    public static final String MONGO_CLIENT_URI = "com.namazustudios.socialengine.mongo.uri";

    public static final String DATABASE_NAME = "com.namazustudios.socialengine.mongo.database.name";

    public IntegrationTestModule(JeroMQEmbeddedTestService embeddedTestService) {
        this.embeddedTestService = embeddedTestService;
    }

    private final Application spyApplication; {
        spyApplication = spy(Application.class);
        spyApplication.setId(randomUUID().toString());
        spyApplication.setName("Test");
        spyApplication.setDescription("Test Application.");
    }

    @Override
    protected void configure() {

        final var mockModule = new MockModule();
        final var applicationId = ApplicationId.forUniqueName(spyApplication.getId());
        final var port = testPort.getAndIncrement();

        mockModule.mock(Client.class);
        mockModule.mock(NotificationBuilder.class);
        mockModule.bind(spyApplication, Key.get(Application.class));

        bind(EmbeddedTestService.class).toInstance(embeddedTestService
                .withClient()
                .withWorkerModule(new MongoTestInstanceModule(port))
                .withWorkerModule(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(String.class)
                                .annotatedWith(Names.named(DATABASE_NAME))
                                .toInstance("test_elements_db");
                        bind(String.class)
                                .annotatedWith(Names.named(MONGO_CLIENT_URI))
                                .toInstance(format("mongodb://%s:%d", TEST_BIND_IP, port));
                    }
                })
                .withApplicationNode(applicationId)
                    .withNodeModules(new ClasspathAssetLoaderModule().withDefaultPackageRoot())
                    .withNodeModules(new LuaModule().visitDiscoveredModule(mockModule::mock))
                    .withNodeModules(mockModule)
                .endApplication()
                .start()
        );

        bind(Context.class).toProvider(() -> {
            final var factory = embeddedTestService.getClient().getContextFactory();
            return factory.getContextForApplication(spyApplication.getId());
        });

    }

    private class MockModule extends AbstractModule {

        private Set<Key<?>> types = new HashSet<>();

        private List<Runnable> bindings = new ArrayList<>();

        @Override
        protected void configure() {
            bindings.forEach(Runnable::run);
        }

        public <T> void mock(final Class<T> type) {

            final var key = Key.get(type);

            if (!type.isEnum() && types.add(key)) {
                final var mock = Mockito.mock(type);
                bind(mock, key);
            }

        }

        public <T> void mock(final ExposedModuleDefinition module, final Class<T> type) {

            if (module.annotation().value() == ExposedBindingAnnotation.Undefined.class) {

                final var key = Key.get(type);

                if (!type.isEnum() && types.add(key)) {
                    final var mock = Mockito.mock(type);
                    bind(mock, key);
                }

            } else {

                final var annotation = ExposedBindingAnnotation.Util.resolve(type, module.annotation());
                final var key = Key.get(type, annotation);

                if (!type.isEnum() && types.add(key)) {
                    final var mock = Mockito.mock(type);
                    bind(mock, key);
                }

            }

        }

        private <T> void bind(final T mock, final Key<T> binding) {

            IntegrationTestModule.this.bind(binding).toInstance(mock);
            bindings.add(() -> {
                logger.info("Binding {} to mock {}", binding, mock);
                bind(binding).toInstance(mock);
            });
        }

    }

}

