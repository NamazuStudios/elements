package dev.getelements.elements.jetty;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.appserve.AppServeDispatcherAppProvider;
import dev.getelements.elements.cdnserve.CdnAppProvider;
import dev.getelements.elements.codeserve.CodeServeAppProvider;
import dev.getelements.elements.docserve.DocAppProvider;
import dev.getelements.elements.formidium.FormidiumAppProvider;
import dev.getelements.elements.rest.RestAPIAppProvider;
import org.eclipse.jetty.deploy.AppProvider;

import java.util.*;

import static dev.getelements.elements.jetty.ElementsWebServiceComponent.*;
import static java.util.stream.Collectors.toUnmodifiableList;

public class ElementsWebServiceComponentModule extends AbstractModule {

    private static final Map<ElementsWebServiceComponent, Class<? extends AppProvider>> PROVIDERS;

    static {
        final var providers = new EnumMap<ElementsWebServiceComponent, Class<? extends AppProvider>>(ElementsWebServiceComponent.class);
        providers.put(api, RestAPIAppProvider.class);
        providers.put(cdn, CdnAppProvider.class);
        providers.put(app, AppServeDispatcherAppProvider.class);
        providers.put(doc, DocAppProvider.class);
        providers.put(code, CodeServeAppProvider.class);
        providers.put(formidium_proxy, FormidiumAppProvider.class);
        PROVIDERS = Collections.unmodifiableMap(providers);
    }

    private final List<ElementsWebServiceComponent> elementsWebServiceComponents;

    public ElementsWebServiceComponentModule() {
        this.elementsWebServiceComponents = List.of(ElementsWebServiceComponent.values());
    }

    public ElementsWebServiceComponentModule(final Collection<ElementsWebServiceComponent> elementsWebServiceComponents) {
        this.elementsWebServiceComponents = elementsWebServiceComponents
            .stream()
            .sorted()
            .collect(toUnmodifiableList());
    }

    @Override
    protected void configure() {

        final var multibinder = Multibinder.newSetBinder(binder(), AppProvider.class);

        elementsWebServiceComponents
                .stream()
                .filter(PROVIDERS::containsKey)
                .map(PROVIDERS::get)
                .forEach(c -> multibinder.addBinding().to(c));

    }

}
