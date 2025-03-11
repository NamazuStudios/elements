package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.record.ElementServiceRecord;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.inject.name.Names.bindProperties;
import static com.google.inject.name.Names.named;
import static java.util.Objects.requireNonNull;

/**
 * Used to bind services.
 */
public class GuiceSpiModule extends PrivateModule {

    private final Set<Class<?>> targets = new HashSet<>();

    private final List<Runnable> bindings = new LinkedList<>();

    private Attributes attributes = Attributes.emptyAttributes();

    @Override
    protected void configure() {

        binder().requireExplicitBindings();
        bindProperties(binder(), attributes.asProperties());

        try {
            bindings.forEach(Runnable::run);
        } finally {
            targets.clear();
        }

    }

    public GuiceSpiModule define(final ElementServiceRecord elementServiceRecord) {

        bindings.add(() -> {

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

        });

        return this;
    }

    public GuiceSpiModule attributes(final Attributes attributes) {
        requireNonNull(attributes, "attributes");
        this.attributes = attributes;
        return this;
    }

}

