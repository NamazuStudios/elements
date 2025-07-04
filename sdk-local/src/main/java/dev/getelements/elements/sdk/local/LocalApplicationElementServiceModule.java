package dev.getelements.elements.sdk.local;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.common.app.ApplicationElementService;
import dev.getelements.elements.common.app.StandardApplicationElementService;

import java.net.URL;
import java.util.List;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.local.LocalApplicationElementService.ELEMENT_CLASSPATH;
import static dev.getelements.elements.sdk.local.LocalApplicationElementService.SDK_CLASSPATH;
import static java.util.List.copyOf;

public class LocalApplicationElementServiceModule extends PrivateModule {

    private final List<URL> sdkClasspath;

    private final List<URL> elementClasspath;

    private final List<LocalApplicationElementRecord> localElements;

    public LocalApplicationElementServiceModule(
            final List<URL> sdkClasspath,
            final List<URL> elementClasspath,
            final List<LocalApplicationElementRecord> localElements) {
        this.sdkClasspath = List.copyOf(sdkClasspath);
        this.elementClasspath = List.copyOf(elementClasspath);
        this.localElements = copyOf(localElements);
    }

    @Override
    protected void configure() {

        expose(ApplicationElementService.class);

        bind(new TypeLiteral<List<URL>>() {})
                .annotatedWith(named(SDK_CLASSPATH))
                .toInstance(sdkClasspath);

        bind(new TypeLiteral<List<URL>>() {})
                .annotatedWith(named(ELEMENT_CLASSPATH))
                .toInstance(elementClasspath);

        bind(new TypeLiteral<List<LocalApplicationElementRecord>>() {})
                .toInstance(localElements);

        bind(StandardApplicationElementService.class)
                .asEagerSingleton();

        bind(ApplicationElementService.class)
                .to(LocalApplicationElementService.class)
                .asEagerSingleton();

    }



}
