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
     * Returns the singular ELM artifact. May be null or empty, indicating that there is no ELM artifact specified.
     * @return the elm artifact
     */
    String elmArtifact();

    /**
     * Returns artifact coordinates making up the Element. May be null or empty, indicating that there is not ELM
     * artifact specified.
     *
     * @return the Element's artifacts
     */
    List<String> elementArtifacts();

    /**
     * Indicate if the deployment is ready. Ready means that there is at least an ELM artifact or list of artifacts
     * specified. Depending on implementation, additional conditions may apply.
     *
     * @return true if ready
     */
    default boolean ready() {
        return elmArtifact() != null && !elmArtifact().isBlank() ||
               elementArtifacts() != null && elementArtifacts().isEmpty();
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
