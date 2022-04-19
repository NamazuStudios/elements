package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.neo.NeoToken;
import com.namazustudios.socialengine.model.blockchain.template.CreateTokenTemplateRequest;
import com.namazustudios.socialengine.model.blockchain.template.TokenTemplate;
import com.namazustudios.socialengine.model.blockchain.template.UpdateTokenTemplateRequest;
import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

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
     * Lists all {@link NeoToken} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link NeoToken} instances
     */
    Pagination<TokenTemplate> getTokenTemplates(int offset, int count);

    /**
     * Fetches a specific {@link TokenTemplate} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param templateId the template ID
     * @return the {@link TokenTemplate}, never null
     */
    TokenTemplate getTokenTemplate(String templateId);

    /**
     * Updates the supplied {@link TokenTemplate}.
     *
     * @param templateId the id of the token to update
     * @param updateTokenTemplateRequest the update request for the token.
     * @return the {@link TokenTemplate} as it was changed by the service.
     */
    TokenTemplate updateTokenTemplate(String templateId, UpdateTokenTemplateRequest updateTokenTemplateRequest);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenTemplateRequest the {@link CreateTokenTemplateRequest} with the information to create
     * @return the {@link TokenTemplate} as it was created by the service.
     */
    TokenTemplate createTokenTemplate(CreateTokenTemplateRequest tokenTemplateRequest);

    /**
     * Creates a new template by cloning an existing {@link TokenTemplate} definition.
     *
     * @param tokenTemplate the {@link TokenTemplate} with the information to clone
     * @return the {@link TokenTemplate} as it was created by the service.
     */
    TokenTemplate cloneTokenTemplate(TokenTemplate tokenTemplate);

    /**
     * Deletes the {@link TokenTemplate} with the supplied templated ID.
     *
     * @param templateId the token template ID.
     */
    void deleteTokenTemplate(String templateId);

}
