package dev.getelements.elements.sdk.local;

/**
 * Creates instances of {@link ElementsLocal} using a record that contains the necessary information to load.
 */
public interface ElementsLocalFactory {

    /**
     * Creates the {@link ElementsLocal} instance.
     * @param record the record containing the information to load
     * @return the {@link ElementsLocal}
     */
    ElementsLocal create(ElementsLocalFactoryRecord record);

}
