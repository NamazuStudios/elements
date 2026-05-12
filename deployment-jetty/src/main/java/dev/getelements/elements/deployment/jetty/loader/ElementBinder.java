package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Attributes.Attribute;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.record.ElementServiceRecord;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import java.util.Optional;
import java.util.function.Supplier;

public class ElementBinder extends AbstractBinder {

    private final Element element;

    public ElementBinder(final Element element) {
        this.element = element;
    }

    @Override
    protected void configure() {

        bindElement(element);

        final var registry = element.getElementRegistry();

        element.getElementRecord()
                .dependencies()
                .stream()
                .flatMap(dep -> dep.findDependencies(registry))
                .distinct()
                .forEach(this::bindElement);

    }

    private void bindElement(final Element source) {

        source.getElementRecord()
                .attributes()
                .stream()
                .filter(a -> a.value() != null)
                .forEach(this::bindAttribute);

        source.getElementRecord()
                .services()
                .forEach(service -> bindService(source, service));

    }

    @SuppressWarnings("unchecked")
    private <T> void bindAttribute(final Attribute<T> attribute) {
        bind(attribute.value())
                .to((Class<T>) attribute.value().getClass())
                .named(attribute.name());
    }

    @SuppressWarnings("unchecked")
    private <T> void bindService(final Element source, final ElementServiceRecord service) {

        final var export = service.export();

        if (!export.expose()) return;

        final var exposedTypes = export.exposed();

        if (exposedTypes.isEmpty()) return;

        final Class<T> primaryType = (Class<T>) exposedTypes.getFirst();

        final Optional<Supplier<T>> found = export.isNamed()
                ? source.getServiceLocator().findInstance(primaryType, export.name())
                : source.getServiceLocator().findInstance(primaryType);

        found.ifPresent(supplier -> {
            final var binding = bindFactory(supplier);
            exposedTypes.forEach(binding::to);
            if (export.isNamed()) binding.named(export.name());
        });

    }

}
