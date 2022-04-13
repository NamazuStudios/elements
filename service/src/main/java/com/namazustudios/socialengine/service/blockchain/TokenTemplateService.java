package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.template.CreateTokenTemplateRequest;
import com.namazustudios.socialengine.model.blockchain.template.TokenTemplate;
import com.namazustudios.socialengine.model.blockchain.template.UpdateTokenTemplateRequest;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

import java.util.List;

/**
 * Manages instances of {@link TokenTemplate}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ExposedModuleDefinition(value = "namazu.elements.service.blockchain.token"),
        @ExposedModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.token",
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
    Pagination<TokenTemplate> getTokens(int offset, int count);

    /**
     * Fetches a specific {@link TokenTemplate} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param templateId the profile ID
     * @return the {@link TokenTemplate}, never null
     */
    TokenTemplate getTokenTemplate(String templateId);

    /**
     * Updates the supplied {@link TokenTemplate}.
     *
     * @param templateId the id of the token template to update
     * @param tokenRequest the token information to update
     * @return the {@link TokenTemplate} as it was changed by the service.
     */
    TokenTemplate updateTokenTemplate(String templateId, UpdateTokenTemplateRequest tokenRequest);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenRequest the {@link CreateTokenTemplateRequest} with the information to create
     * @return the {@link TokenTemplate} as it was created by the service.
     */
    TokenTemplate createTokenTemplate(CreateTokenTemplateRequest tokenRequest);

    /**
     * Deletes the {@link TokenTemplate} with the supplied token ID.
     *
     * @param templateId the token template ID.
     */
    void deleteTokenTemplate(String templateId);

}
