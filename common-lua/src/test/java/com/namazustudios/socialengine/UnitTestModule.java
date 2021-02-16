package com.namazustudios.socialengine;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.rt.lua.guice.JeroMQEmbeddedTestService;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import com.namazustudios.socialengine.service.NotificationBuilder;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.lang.annotation.Annotation;
import java.util.*;

import static com.namazustudios.socialengine.rt.Context.REMOTE;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.spy;

public class UnitTestModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(UnitTestModule.class);

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService();

    private final Application spyApplication; {
        spyApplication = spy(Application.class);
        spyApplication.setId(randomUUID().toString());
        spyApplication.setName("Test");
        spyApplication.setDescription("Test Application.");
    }

    @Override
    protected void configure() {

        final MockModule mockModule = new MockModule();

        mockModule.mock(Client.class);
        mockModule.mock(NotificationBuilder.class);
        mockModule.bind(spyApplication, Key.get(Application.class));

        bind(JeroMQEmbeddedTestService.class).toInstance(embeddedTestService
            .withWorkerModule(new LuaModule()
                .visitDiscoveredModule(mockModule::mock))
            .withWorkerModule(mockModule)
            .withWorkerModule(new XodusEnvironmentModule().withTempSchedulerEnvironment().withTempResourceEnvironment())
            .start());

        bind(Context.class).toProvider(() -> embeddedTestService
            .getClientIocResolver()
            .inject(Context.class, REMOTE)
        );

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

            UnitTestModule.this.bind(binding).toInstance(mock);
            bindings.add(() -> {
                logger.info("Binding {} to mock {}", binding, mock);
                bind(binding).toInstance(mock);
            });
        }

    }

}
