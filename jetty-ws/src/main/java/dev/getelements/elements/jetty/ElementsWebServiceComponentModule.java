package dev.getelements.elements.jetty;

import com.google.inject.PrivateModule;
import dev.getelements.elements.app.serve.guice.AppServeModule;
import dev.getelements.elements.common.app.ApplicationDeploymentService;
import dev.getelements.elements.guice.StandardServletSecurityModule;
import dev.getelements.elements.guice.StandardServletServicesModule;
import org.eclipse.jetty.server.Handler;

import java.util.Collection;
import java.util.List;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.common.app.ApplicationDeploymentService.APP_NODE;
import static dev.getelements.elements.common.app.ApplicationDeploymentService.APP_SERVE;
import static dev.getelements.elements.jetty.ElementsWebServiceComponent.app_node;
import static dev.getelements.elements.jetty.ElementsWebServiceComponent.app_serve;
import static java.util.stream.Collectors.toUnmodifiableList;


public class ElementsWebServiceComponentModule extends PrivateModule {

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

        expose(Handler.class);

        install(new ElementsServletContextModule());
        install(new StandardServletServicesModule());
        install(new ElementsServletModule(this.elementsWebServiceComponents));

        if (elementsWebServiceComponents.contains(app_serve)) {
            install(new AppServeModule());
            expose(ApplicationDeploymentService.class).annotatedWith(named(APP_SERVE));
        }

    }

}
