package dev.getelements.elements.sdk.model.system;

import java.util.List;

import static dev.getelements.elements.sdk.model.system.ElementDeploymentState.ENABLED;
import static dev.getelements.elements.sdk.model.system.ElementDeploymentState.UNLOADED;

/**
 * A purpose built interface which defines common operations pertinent to deploying an Element.
 */
public interface ElementDeploymentRequest {

    /**
     * Indicates the requested state for the deployment. May be null.
     *
     * @return the state
     */
    ElementDeploymentState state();

    /**
     * Returns the list of Element definitions. May be null or empty, indicating that there are no Element
     * definitions specified.
     *
     * @return the Element definitions
     */
    List<ElementDefinition> elements();

    /**
     * Indicate if the deployment is ready. Ready means that there is at least one Element definition specified.
     * Depending on implementation, additional conditions may apply.
     *
     * @return true if ready
     */
    default boolean ready() {
        return elements() != null && !elements().isEmpty();
    }

    /**
     * Returns the effective state of the deployment based on the state of the request. If the deployment is ready,
     * which is to say that there exists either an ELM artifact or a list of artifacts, then the state will be
     *
     * @return the effective state, never null
     */
    default ElementDeploymentState effectiveState() {
        if (ready()) {
            return state() == null || UNLOADED.equals(state()) ? ENABLED : state();
        } else {
            return UNLOADED;
        }
    }

}
