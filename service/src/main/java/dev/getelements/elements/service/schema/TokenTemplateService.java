package dev.getelements.elements.service.schema;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.schema.template.CreateTokenTemplateRequest;
import dev.getelements.elements.model.schema.template.TokenTemplate;
import dev.getelements.elements.model.schema.template.UpdateTokenTemplateRequest;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.Unscoped;

/**
 * Manages instances of {@link TokenTemplate}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.blockchain.metadata.tokentemplate"),
    @ModuleDefinition(
            value = "namazu.elements.service.blockchain.unscoped.metadata.tokentemplate",
            annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
public interface TokenTemplateService {

    /**
     * Lists all {@link TokenTemplate} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link TokenTemplate} instances
     */
    Pagination<TokenTemplate> getTokenTemplates(int offset, int count);

    /**
     * Fetches a specific {@link TokenTemplate} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param templateIdOrName the profile ID
     * @return the {@link TokenTemplate}, never null
     */
    TokenTemplate getTokenTemplate(String templateIdOrName);

    /**
     * Updates the supplied {@link TokenTemplate}.
     *
     * @param tokenTemplateId the id of the metadata spec to update
     * @param tokenTemplateRequest the token information to update
     * @return the {@link TokenTemplate} as it was changed by the service.
     */
    TokenTemplate updateTokenTemplate(String tokenTemplateId, UpdateTokenTemplateRequest tokenTemplateRequest);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenTemplateRequest the {@link CreateTokenTemplateRequest} with the information to create
     * @return the {@link TokenTemplate} as it was created by the service.
     */
    TokenTemplate createTokenTemplate(CreateTokenTemplateRequest tokenTemplateRequest);

    /**
     * Deletes the {@link TokenTemplate} with the supplied token template ID.
     *
     * @param tokenTemplateId the token template ID.
     */
    void deleteTokenTemplate(String tokenTemplateId);

}
