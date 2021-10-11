package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateTokenRequest;
import com.namazustudios.socialengine.model.blockchain.Token;

import java.util.List;

public interface TokenDao {

    /**
     * Lists all {@link Token} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param tags
     * @param search - name or type
     * @return a {@link Pagination} of {@link Token} instances
     */
    Pagination<Token> getTokens(int offset, int count, List<String> tags, String search);

    /**
     * Fetches a specific {@link Token} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param tokenIdOrName the profile ID
     * @return the {@link Token}, never null
     */
    Token getToken(String tokenIdOrName);

    /**
     * Updates the supplied {@link Token}.
     *
     * @param tokenId the id of the token with the information to update
     * @return the {@link Token} as it was changed by the service.
     */
    Token updateToken(String tokenId);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenRequest the {@link CreateTokenRequest} with the information to create
     * @return the {@link Token} as it was created by the service.
     */
    Token createToken(CreateTokenRequest tokenRequest);

    /**
     * Deletes the {@link Token} with the supplied profile ID.
     *
     * @param templateId the template ID.
     */
    void deleteToken(String templateId);
}
