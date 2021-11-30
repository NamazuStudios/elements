package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateNeoTokenRequest;
import com.namazustudios.socialengine.model.blockchain.NeoToken;
import com.namazustudios.socialengine.model.blockchain.UpdateNeoTokenRequest;

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
     * @param tokenId the id of the token to update
     * @param updateNeoTokenRequest the update request for the token.
     * @return the {@link NeoToken} as it was changed by the service.
     */
    NeoToken updateToken(String tokenId, UpdateNeoTokenRequest updateNeoTokenRequest);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenRequest the {@link CreateNeoTokenRequest} with the information to create
     * @return the {@link NeoToken} as it was created by the service.
     */
    NeoToken createToken(CreateNeoTokenRequest tokenRequest);

    /**
     * Deletes the {@link NeoToken} with the supplied profile ID.
     *
     * @param tokenId the neo token ID.
     */
    void deleteToken(String tokenId);
}
