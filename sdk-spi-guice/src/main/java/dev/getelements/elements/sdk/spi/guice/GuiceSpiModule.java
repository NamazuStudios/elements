package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation.DefaultImplementation;
import dev.getelements.elements.sdk.spi.guice.record.GuiceElementModuleRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementServiceRecord;
import jakarta.inject.Provider;

import java.util.HashSet;
import java.util.Set;

import static com.google.inject.name.Names.bindProperties;
import static com.google.inject.name.Names.named;
import static java.util.Objects.requireNonNull;

/**
 * Used to bind services.
 */
public class GuiceSpiModule extends PrivateModule {

    private final ElementRegistry parent;

    private final ElementRecord elementRecord;

    public GuiceSpiModule(
            final ElementRegistry parent,
            final ElementRecord elementRecord) {
        this.parent = requireNonNull(parent, "parent");
        this.elementRecord = requireNonNull(elementRecord, "elementRecord");
    }

    @Override
    protected void configure() {

        final var attributes = elementRecord.attributes().asProperties();

        binder().requireExplicitBindings();
        bindProperties(binder(), attributes);

        final var targets  = new HashSet<Class<?>>();

        elementRecord
                .services()
                .stream()
                .filter(esr -> DefaultImplementation.class.equals(esr.implementation().type()))
                .forEach(this::exposeService);

        elementRecord
                .services()
                .stream()
                .filter(esr -> !DefaultImplementation.class.equals(esr.implementation().type()))
                .forEach(esr -> bindAndExposeService(targets, esr));

        elementRecord
                .dependencies()
                .stream()
                .flatMap(dep -> dep.findDependencies(parent))
                .forEach(this::bindDependentElement);

        final var elementPackage = elementRecord.definition().pkg();

        GuiceElementModuleRecord
                .fromPackage(elementPackage)
                .map(GuiceElementModuleRecord::newModule)
                .forEach(this::install);

    }

    private void exposeService(final ElementServiceRecord elementServiceRecord) {

        final var export = elementServiceRecord.export();

        final var keys = export.isNamed()
                ? export.exposed().stream().map(anInterface -> Key.get(anInterface, named(export.name())))
                : export.exposed().stream().map(Key::get);

        keys.forEach(this::expose);

    }

    private void bindAndExposeService(final Set<Class<?>> targets,
                                      final ElementServiceRecord elementServiceRecord) {

        final var export = elementServiceRecord.export();
        final var implementation = elementServiceRecord.implementation();

        if (targets.add(implementation.type())) {

            bind(implementation.type());

            if (implementation.expose()) {
                expose(implementation.type());
            }

        }

        final var keys = export.isNamed()
                ? export.exposed().stream().map(anInterface -> Key.get(anInterface, named(export.name())))
                : export.exposed().stream().map(Key::get);

        keys.forEach(k -> {
            bind(k).to((Class)implementation.type());
            expose(k);
        });

    }

    private void bindDependentElement(final Element element) {
        element.getElementRecord()
                .services()
                .forEach(esr -> bindDependentService(element, esr));
    }

    private void bindDependentService(
            final Element element,
            final ElementServiceRecord elementServiceRecord) {

        final var export = elementServiceRecord.export();

        export.exposed()
                .stream()
                .forEach(aClass -> {

                    final Key<Object> key = export.isNamed()
                            ? (Key<Object>) Key.get(aClass, named(export.name()))
                            : (Key<Object>) Key.get(aClass);

                    final Provider<Object> provider = () -> export.isNamed()
                            ? element.getServiceLocator().getInstance(aClass, export.name())
                            : element.getServiceLocator().getInstance(aClass);

                    bind(key).toProvider(provider);

                });
    }

}
