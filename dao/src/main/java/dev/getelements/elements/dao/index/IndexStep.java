package dev.getelements.elements.dao.index;

import dev.getelements.elements.model.schema.template.TemplateTab;
import dev.getelements.elements.model.schema.template.TemplateTabField;

/**
 * Represents a step in the plan.
 */
public interface IndexStep {

    /**
     * Gets the index name representative of the path.
     *
     * @return the unique name of the index
     */
    String getIndexName();

    /**
     * Gets the specific {@link TemplateTabField} which is relevant index step.
     *
     * @return the {@link TemplateTabField}
     */
    TemplateTabField getTemplateTabField();

    /**
     * A human-readable description of this index step.
     *
     * @return the description
     */
    String getDescription();

}
