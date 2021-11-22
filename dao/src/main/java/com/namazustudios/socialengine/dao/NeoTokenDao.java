package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateTokenRequest;
import com.namazustudios.socialengine.model.blockchain.NeoToken;
import com.namazustudios.socialengine.model.blockchain.UpdateTokenRequest;

import java.util.List;

public interface NeoTokenDao {

    /**
     * Lists all {@link NeoToken} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param tags
     * @param search - name or type
     * @return a {@link Pagination} of {@link NeoToken} instances
     */
    Pagination<NeoToken> getTokens(int offset, int count, List<String> tags, String search);

    /**
     * Fetches a specific {@link NeoToken} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param tokenIdOrName the profile ID
     * @return the {@link NeoToken}, never null
     */
    NeoToken getToken(String tokenIdOrName);

    /**
     * Updates the supplied {@link NeoToken}.
     *
     * @param updateTokenRequest the update request for the token.
     * @return the {@link NeoToken} as it was changed by the service.
     */
    NeoToken updateToken(UpdateTokenRequest updateTokenRequest);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenRequest the {@link CreateTokenRequest} with the information to create
     * @return the {@link NeoToken} as it was created by the service.
     */
    NeoToken createToken(CreateTokenRequest tokenRequest);

    /**
     * Deletes the {@link NeoToken} with the supplied profile ID.
     *
     * @param templateId the template ID.
     */
    void deleteToken(String templateId);
}
