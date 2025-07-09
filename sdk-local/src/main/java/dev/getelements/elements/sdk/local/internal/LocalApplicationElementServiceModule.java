package dev.getelements.elements.sdk.local.internal;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.common.app.ApplicationElementService;
import dev.getelements.elements.common.app.StandardApplicationElementService;
import dev.getelements.elements.sdk.local.ElementsLocalApplicationElementRecord;

import java.util.List;

import static java.util.List.copyOf;

public class LocalApplicationElementServiceModule extends PrivateModule {

    private final List<ElementsLocalApplicationElementRecord> localElements;

    public LocalApplicationElementServiceModule(final List<ElementsLocalApplicationElementRecord> localElements) {
        this.localElements = copyOf(localElements);
    }

    @Override
    protected void configure() {

        expose(ApplicationElementService.class);

        bind(new TypeLiteral<List<ElementsLocalApplicationElementRecord>>() {})
                .toInstance(localElements);

        bind(StandardApplicationElementService.class)
                .asEagerSingleton();

        bind(ApplicationElementService.class)
                .to(LocalApplicationElementService.class)
                .asEagerSingleton();

    }



}
