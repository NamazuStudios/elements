package dev.getelements.elements;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.appserve.DispatcherAppProvider;
import dev.getelements.elements.cdnserve.CdnAppProvider;
import dev.getelements.elements.codeserve.CodeServeAppProvider;
import dev.getelements.elements.docserve.DocAppProvider;
import dev.getelements.elements.formidium.FormidiumAppProvider;
import dev.getelements.elements.rest.RestAPIAppProvider;
import org.eclipse.jetty.deploy.AppProvider;

import java.util.*;

import static dev.getelements.elements.ElementsWebService.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

public class ElementsWebServiceModule extends AbstractModule {

    private static final Map<ElementsWebService, Class<? extends AppProvider>> PROVIDERS;

    static {
        final var providers = new EnumMap<ElementsWebService, Class<? extends AppProvider>>(ElementsWebService.class);
        providers.put(api, RestAPIAppProvider.class);
        providers.put(cdn, CdnAppProvider.class);
        providers.put(app, DispatcherAppProvider.class);
        providers.put(doc, DocAppProvider.class);
        providers.put(code, CodeServeAppProvider.class);
        providers.put(formidium_proxy, FormidiumAppProvider.class);
        PROVIDERS = Collections.unmodifiableMap(providers);
    }

    private final List<ElementsWebService> elementsWebServices;

    public ElementsWebServiceModule(final Collection<ElementsWebService> elementsWebServices) {
        this.elementsWebServices = elementsWebServices
            .stream()
            .sorted()
            .collect(toUnmodifiableList());
    }

    @Override
    protected void configure() {

        final var multibinder = Multibinder.newSetBinder(binder(), AppProvider.class);

        elementsWebServices
                .stream()
                .filter(PROVIDERS::containsKey)
                .map(PROVIDERS::get)
                .forEach(c -> multibinder.addBinding().to(c));

    }

}
