package dev.getelements.elements;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.rt.guice.ClasspathAssetLoaderModule;
import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.rt.lua.guice.LuaModule;
import dev.getelements.elements.service.NotificationBuilder;
import dev.getelements.elements.test.EmbeddedTestService;
import dev.getelements.elements.test.JeroMQEmbeddedTestService;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.spy;

public class UnitTestModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(UnitTestModule.class);

    private final JeroMQEmbeddedTestService embeddedTestService;

    public UnitTestModule(JeroMQEmbeddedTestService embeddedTestService) {
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

        mockModule.mock(Client.class);
        mockModule.mock(NotificationBuilder.class);
        mockModule.bind(spyApplication, Key.get(Application.class));

        bind(EmbeddedTestService.class).toInstance(embeddedTestService
            .withClient()
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

        public <T> void mock(final ModuleDefinition module, final Class<T> type) {

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
