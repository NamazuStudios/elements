package dev.getelements.elements.app.serve.loader;

import dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import dev.getelements.elements.sdk.Element;

@FunctionalInterface
public interface Loader {

    /**
     * Loads the {@link Element}s from the supplied {@link ApplicationElementRecord}
     *
     * @param record the record to load
     */
    default void load(ApplicationElementRecord record) {
        record.elements().stream().forEach(element -> load(record, element));
    }

    /**
     * Loads the specific {@link Element} from the supplied {@link ApplicationElementRecord}
     *
     * @param record the record to load
     */
    void load(ApplicationElementRecord record, Element element);

}
