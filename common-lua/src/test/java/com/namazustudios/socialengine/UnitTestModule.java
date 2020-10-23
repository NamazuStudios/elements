package com.namazustudios.socialengine;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.lua.guice.JeroMQEmbeddedTestService;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.service.NotificationBuilder;
import org.mockito.Mockito;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.Mockito.spy;

public class UnitTestModule extends AbstractModule {

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
        mockModule.bind(spyApplication, Application.class);

        bind(JeroMQEmbeddedTestService.class).toInstance(embeddedTestService
            .withWorkerModule(new LuaModule().visitDiscoveredExtension((m, c) -> mockModule.mock(c)))
            .withWorkerModule(mockModule)
//            .withWorkerModule(new XodusContextModule()
//                .withSchedulerThreads(1)
//                .withHandlerTimeout(3, MINUTES))
//            .withWorkerModule(new XodusEnvironmentModule()
//                .withTempEnvironments())
            .start());

        bind(Context.class).toProvider(embeddedTestService::getContext);

    }

    private class MockModule extends AbstractModule {

        private Set<Class<?>> types = new HashSet<>();

        private List<Runnable> bindings = new ArrayList<>();

        @Override
        protected void configure() {
            bindings.forEach(runnable -> runnable.run());
        }

        public <T> void mock(final Class<T> type) {
            if (!type.isEnum() && types.add(type)) {
                final T mock = Mockito.mock(type);
                bind(mock, type);
            }
        }

        private <T> void bind(final T mock, final Class<T> binding) {
            UnitTestModule.this.bind(binding).toInstance(mock);
            bindings.add(() -> bind(binding).toInstance(mock));
        }

    }

}
