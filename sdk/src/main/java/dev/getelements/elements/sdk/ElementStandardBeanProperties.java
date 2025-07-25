package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;

/**
 * A set of standard Java Bean properties which will be injected into components as-needed.
 */
public interface ElementStandardBeanProperties {

    /**
     * The {@link ElementRecord} bean property.
     */
    String ELEMENT_RECORD = "elementRecord";

    /**
     * The {@link ElementDefinitionRecord} bean property.
     */
    String ELEMENT_DEFINITION_RECORD = "elementDefinitionRecord";

    /**
     * The {@link ElementRecord} bean property.
     */
    String SERVICE_LOCATOR = "serviceLocator";

    /**
     * The {@link ElementRegistry} bean property.
     */
    String ELEMENT_REGISTRY = "elementRegistry";

    /**
     * The {@link StandardBeanProperty} for the {@link ElementRecord}.
     */
    StandardBeanProperty<ElementRecord> ELEMENT_RECORD_PROPERTY = new StandardBeanProperty<>(
            ElementRecord.class,
            ELEMENT_RECORD
    );

    /**
     * The {@link StandardBeanProperty} for the {@link ElementDefinitionRecord}.
     */
    StandardBeanProperty<ElementDefinitionRecord> ELEMENT_DEFINITION_RECORD_PROPERTY =
            new StandardBeanProperty<>(
                    ElementDefinitionRecord.class,
                    ELEMENT_DEFINITION_RECORD
            );

    /**
     * The {@link StandardBeanProperty} for the {@link ServiceLocator}.
     */
    StandardBeanProperty<ServiceLocator> SERVICE_LOCATOR_PROPERTY = new StandardBeanProperty<>(
            ServiceLocator.class,
            SERVICE_LOCATOR
    );

    /**
     * The {@link StandardBeanProperty} for the {@link ElementRegistry}
     */
    StandardBeanProperty<ElementRegistry> ELEMENT_REGISTRY_PROPERTY = new StandardBeanProperty<>(
            ElementRegistry.class,
            ELEMENT_REGISTRY
    );

    /**
     * Defines a bean property.
     *
     * @param type the property type
     * @param name the property name
     * @param <T>
     */
    record StandardBeanProperty<T>(Class<T> type, String name) {}

}
