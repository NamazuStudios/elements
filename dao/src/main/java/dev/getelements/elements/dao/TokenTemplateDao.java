package dev.getelements.elements.dao;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.neo.NeoToken;
import dev.getelements.elements.model.schema.template.CreateTokenTemplateRequest;
import dev.getelements.elements.model.schema.template.TokenTemplate;
import dev.getelements.elements.model.schema.template.UpdateTokenTemplateRequest;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Created by garrettmcspadden on 11/23/21.
 */
@Expose({
    @ModuleDefinition("namazu.elements.dao.token.template"),
    @ModuleDefinition(
        value = "namazu.socialengine.dao.token.template",
        deprecated = @DeprecationDefinition("Use namazu.elements.dao.token.template instead")
    )
})
public interface TokenTemplateDao {

    /**
     * Lists all {@link TokenTemplate} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link NeoToken} instances
     */
    Pagination<TokenTemplate> getTokenTemplates(int offset, int count, String userId);

    /**
     * Fetches a specific {@link TokenTemplate} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param tokenTemplateId the template ID
     * @return the {@link TokenTemplate}, never null
     */
    TokenTemplate getTokenTemplate(String tokenTemplateId, String userId);

    /**
     * Updates the supplied {@link TokenTemplate}.
     *
     * @param tokenTemplateId the id of the token to update
     * @param updateTokenTemplateRequest the update request for the metaDataSpec.
     * @return the {@link TokenTemplate} as it was changed by the service.
     */
    TokenTemplate updateTokenTemplate(String tokenTemplateId, UpdateTokenTemplateRequest updateTokenTemplateRequest);

    /**
     * Creates a new token template.
     *
     * @param createTokenTemplateRequest the {@link CreateTokenTemplateRequest} with the information to create
     * @return the {@link TokenTemplate} as it was created by the service.
     */
    TokenTemplate createTokenTemplate(CreateTokenTemplateRequest createTokenTemplateRequest);

    /**
     * Creates a new template by cloning an existing {@link TokenTemplate} definition.
     *
     * @param tokenTemplate the {@link TokenTemplate} with the information to clone
     * @return the {@link TokenTemplate} as it was created by the service.
     */
    TokenTemplate cloneTokenTemplate(TokenTemplate tokenTemplate);

    /**
     * Deletes the {@link TokenTemplate} with the supplied tokenTemplate ID.
     *
     * @param tokenTemplateId the tokenTemplate ID.
     */
    void deleteTokenTemplate(String tokenTemplateId);
}
