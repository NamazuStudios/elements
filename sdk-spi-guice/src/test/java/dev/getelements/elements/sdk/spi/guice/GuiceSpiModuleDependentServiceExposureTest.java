package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.Guice;
import dev.getelements.elements.sdk.guice.GuiceServiceLocator;
import dev.getelements.elements.sdk.record.ElementServiceKey;
import dev.getelements.elements.sdk.spi.DefaultElementLoaderFactory;
import dev.getelements.elements.sdk.spi.RootElementRegistry;
import dev.getelements.elements.sdk.spi.guice.fixture.dependency.dependent.DependentTestService;
import dev.getelements.elements.sdk.spi.guice.fixture.dependency.provider.DependencyTestService;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Proves the {@code @ElementDependency} services bound by {@link GuiceSpiModule#bindDependentService} are exposed to
 * the element's own {@link dev.getelements.elements.sdk.ServiceLocator} (issue #112). Because {@link GuiceSpiModule}
 * is a {@link com.google.inject.PrivateModule}, bindings that are not {@code expose()}d are invisible to
 * {@link dev.getelements.elements.sdk.guice.GuiceServiceLocator#findInstance}, so an element's endpoint code cannot
 * resolve the SDK services it depends on.
 */
public class GuiceSpiModuleDependentServiceExposureTest {

    private final DefaultElementLoaderFactory factory = new DefaultElementLoaderFactory();

    @Test
    public void dependentElementServiceLocatorResolvesDependencyServices() {

        final var parent = new RootElementRegistry();

        final var providerRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                DependencyTestService.class.getPackage()
        );

        final var providerInjector = Guice.createInjector(new GuiceSpiModule(parent, providerRecord));
        final var providerLocator = new GuiceServiceLocator();
        providerLocator.setInjector(providerInjector);

        parent.register(new GuiceSdkElement(providerRecord, providerLocator, parent));

        final var dependentRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                DependentTestService.class.getPackage()
        );

        final var dependentInjector = Guice.createInjector(new GuiceSpiModule(parent, dependentRecord));
        final var dependentLocator = new GuiceServiceLocator();
        dependentLocator.setInjector(dependentInjector);

        final var own = dependentLocator.findInstance(new ElementServiceKey<>(DependentTestService.class, ""));
        assertTrue(own.isPresent());
        assertEquals(own.orElseThrow().get().get(), "dependent");

        final var dependency = dependentLocator.findInstance(new ElementServiceKey<>(DependencyTestService.class, ""));
        assertTrue(dependency.isPresent());
        assertEquals(dependency.orElseThrow().get().get(), "provider");

    }

}