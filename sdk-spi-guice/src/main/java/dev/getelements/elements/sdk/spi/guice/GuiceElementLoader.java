package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Stage;
import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.annotation.ElementPackage;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementService;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.guice.GuiceServiceLocatorModule;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.spi.ElementScopedElementRegistry;
import dev.getelements.elements.sdk.spi.ElementScopedElementRegistrySupplier;
import dev.getelements.elements.sdk.spi.ElementScopedElementSupplier;
import dev.getelements.elements.sdk.spi.SpiUtilities;

import static dev.getelements.elements.sdk.ElementType.ISOLATED_CLASSPATH;

/**
 * The {@link GuiceElementLoader} uses Guice to scan and instantiate an SDK element using a guice element with the
 * following strategy.
 *
 * <ul>
 *     <li>Guice, by default honors the {@link jakarta.inject.Singleton} annotation.</li>
 *     <li>Each type will be explicitly bound via the {@link ElementService} annotation.</li>
 *     <li>Each specification of {@link ElementServiceExport} will expose the the service exposed.</li>
 * </ul>
 *
 * <p>Element injectors are built with {@link Stage#PRODUCTION}, so every {@code @Singleton}-scoped binding
 * (not just explicit {@code .asEagerSingleton()} bindings) is constructed at Element-load time rather than
 * on first use, and every binding is eagerly validated up front (a misconfigured binding fails fast at load
 * time, not at first invocation). Because of this, a service class an Element author marks
 * {@code @Singleton} must obtain any shared mutable dependency (e.g. the platform's Mongo {@code Datastore})
 * through the SDK-provided live-delegating proxy/{@code Provider} rather than a raw injected reference —
 * otherwise eager construction can freeze a stale snapshot of that dependency (the failure mode from a past
 * incident where a raw {@code Datastore} was frozen into an eager singleton while the shared reference it
 * came from was later swapped for a fresh one).</p>
 */
public class GuiceElementLoader implements ElementLoader {

    private ElementRecord elementRecord;

    @Override
    public Element load(final MutableElementRegistry parent) {

        final var injector = Guice.createInjector(
                Stage.PRODUCTION,
                newCoreModule(parent),
                new GuiceServiceLocatorModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {

                        final var provider = getProvider(Element.class);

                        bind(ElementRegistry.class)
                                .toProvider(() -> new ElementScopedElementRegistry(parent, provider.get()))
                                .asEagerSingleton();

                        bind(ElementRecord.class)
                                .toInstance(getElementRecord());

                        bind(Element.class)
                                .to(GuiceSdkElement.class)
                                .asEagerSingleton();

                    }
                }
        );

        final var element = injector.getInstance(Element.class);

        if (element.getElementRecord().type().equals(ISOLATED_CLASSPATH)) {

            final var registry = injector.getInstance(ElementRegistry.class);
            final var elementClassLoader = getElementRecord().classLoader();

            SpiUtilities.getInstance().bind(
                    elementClassLoader,
                    ElementScopedElementSupplier.class,
                    element,
                    Element.class
            );

            SpiUtilities.getInstance().bind(
                    elementClassLoader,
                    ElementScopedElementRegistrySupplier.class,
                    registry,
                    ElementRegistry.class
            );

        }

        final var event = Event.builder()
                .named(SYSTEM_EVENT_ELEMENT_LOADED)
                .build();

        element.publish(event);

        return element;

    }

    private com.google.inject.Module newCoreModule(final ElementRegistry parent) {
        return switch (getElementRecord().type()) {
            case ISOLATED_CLASSPATH -> new GuiceSpiModule(parent, getElementRecord());
            default -> throw new SdkException("Unsupported type : " + getElementRecord().type());
        };
    }

    @Override
    public ElementRecord getElementRecord() {
        return elementRecord;
    }

    public void setElementRecord(ElementRecord elementRecord) {
        this.elementRecord = elementRecord;
    }

}
