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

        element.getElementRecord()
                .attributes()
                .stream()
                .filter(a -> a.value() != null)
                .forEach(this::bindAttribute);

        element.getElementRecord()
                .services()
                .forEach(this::bindService);

    }

    @SuppressWarnings("unchecked")
    private <T> void bindAttribute(final Attribute<T> attribute) {
        bind(attribute.value())
                .to((Class<T>) attribute.value().getClass())
                .named(attribute.name());
    }

    @SuppressWarnings("unchecked")
    private <T> void bindService(final ElementServiceRecord service) {

        final var export = service.export();
        final var exposedTypes = export.exposed();

        if (exposedTypes.isEmpty()) return;

        final Class<T> primaryType = (Class<T>) exposedTypes.getFirst();

        final Optional<Supplier<T>> found = export.isNamed()
                ? element.getServiceLocator().findInstance(primaryType, export.name())
                : element.getServiceLocator().findInstance(primaryType);

        found.ifPresent(supplier -> {
            final var binding = bindFactory(supplier);
            exposedTypes.forEach(binding::to);
            if (export.isNamed()) binding.named(export.name());
        });

    }

}
