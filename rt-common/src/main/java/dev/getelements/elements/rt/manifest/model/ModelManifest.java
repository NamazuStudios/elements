package dev.getelements.elements.rt.manifest.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by patricktwohig on 8/16/17.
 */
public class ModelManifest implements Serializable {

    @Valid
    @NotNull
    private Map<@NotNull String, @NotNull Model> modelsByName;

    /**
     * Gets a mapping of {@link Model} instance by name.
     *
     * @return the {@link Map<String, Model>}
     */
    public Map<String, Model> getModelsByName() {
        return modelsByName;
    }

    /**
     * Sets the ampping of {@link Model} by name.
     * @param modelsByName
     */
    public void setModelsByName(Map<String, Model> modelsByName) {
        this.modelsByName = modelsByName;
    }

}
