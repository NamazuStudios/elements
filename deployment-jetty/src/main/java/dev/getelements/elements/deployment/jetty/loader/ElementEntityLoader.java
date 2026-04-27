package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.dao.ElementEntityRegistrar;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Loader} that registers an element's entity classes with the platform database mapper
 * on load and deregisters them on unload.
 *
 * <p>This loader is independent of the RS and WS loaders so that entity registration applies
 * to all element types uniformly. It is an optional binding: if no {@link ElementEntityRegistrar}
 * is bound (e.g. in non-Mongo deployments), both operations are silently skipped.
 */
public class ElementEntityLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(ElementEntityLoader.class);

    private ElementEntityRegistrar elementEntityRegistrar;

    @Override
    public void load(final PendingDeployment pending, final RuntimeRecord record, final Element element) {
        if (elementEntityRegistrar != null) {
            elementEntityRegistrar.registerEntityClasses(element);
        }
    }

    @Override
    public void unload(final Element element) {
        if (elementEntityRegistrar != null) {
            elementEntityRegistrar.unregisterEntityClasses(element);
        } else {
            logger.debug("No ElementEntityRegistrar bound; skipping entity deregistration for {}",
                    element.getElementRecord().definition().name());
        }
    }

    public ElementEntityRegistrar getElementEntityRegistrar() {
        return elementEntityRegistrar;
    }

    @Inject
    public void setElementEntityRegistrar(final ElementEntityRegistrar elementEntityRegistrar) {
        this.elementEntityRegistrar = elementEntityRegistrar;
    }

}
