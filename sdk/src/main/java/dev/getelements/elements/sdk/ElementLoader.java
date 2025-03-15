package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ElementRecord;

import static dev.getelements.elements.sdk.ElementLoader.SYSTEM_EVENT_ELEMENT_LOADED;

/**
 * Interface for loading SDK Elements. Implementations of this amy implement the following Java bean properties, which
 * will be injected by the {@link ElementLoaderFactory} instance using the bean property "elementRecord".
 *
 * Note, {@link ElementLoader} instances are meant to be one-time use. Once {@link #load()} or related methods are
 * called then subsequent calls are undefined.
 *
 *
 */
@ElementEventProducer(
        value = SYSTEM_EVENT_ELEMENT_LOADED,
        description = "Called by the ElementLoader to indicate that the Element was loaded."
)
public interface ElementLoader {

    /**
     * The {@link ElementRecord} bean property.
     */
    String ELEMENT_RECORD = "elementRecord";

    /**
     * The {@link ElementRecord} bean property.
     */
    String SERVICE_LOCATOR = "serviceLocator";

    /**
     * Called after the Element is loaded.
     */
    String SYSTEM_EVENT_ELEMENT_LOADED = "dev.getelements.element.loaded";

    /**
     * Loads a new instance of the {@link Element} without a parent registry.
     */
    default Element load() {
        return load(ElementRegistry.newDefaultInstance());
    }

    /**
     * Loads a new instance of the {@link Element} with the supplied {@link ElementRegistry} as the parent.
     *
     * @return the {@link Element}, loaded
     */
    Element load(ElementRegistry parent);

    /**
     * Gets the {@link ElementRecord}, which can be invoked before attepting a {@link #load()} or
     * {@link #load(ElementRegistry)} operation.
     *
     * @return the {@link ElementRecord}
     */
    ElementRecord getElementRecord();

    /**
     * Used to specify the system default {@link ElementLoader}. When the {@link ElementDefinition} specifies this,
     * Elements will provide the {@link ElementLoader} implementation via SPI.
     */
    class Default implements ElementLoader {

        private Default() {}

        @Override
        public Element load() {
            throw new SdkException("Not Implemented.");
        }

        @Override
        public Element load(ElementRegistry parent) {
            throw new SdkException("Not Implemented.");
        }

        @Override
        public ElementRecord getElementRecord() {
            throw new SdkException("Not Implemented.");
        }

    }

}
