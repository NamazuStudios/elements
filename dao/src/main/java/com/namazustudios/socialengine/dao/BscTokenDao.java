package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscTokenRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscToken;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscTokenRequest;
import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.List;

/**
 * Created by TuanTran on 3/24/22.
 */
@Expose({
    @ModuleDefinition("namazu.elements.dao.bsc.token"),
    @ModuleDefinition(
        value = "namazu.socialengine.dao.bsc.token",
        deprecated = @DeprecationDefinition("Use namazu.elements.dao.bsc.token instead"))
})
public interface BscTokenDao {

    /**
     * Lists all {@link BscToken} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param tags
     * @param mintStatus
     * @param search - name or type
     * @return a {@link Pagination} of {@link BscToken} instances
     */
    Pagination<BscToken> getTokens(int offset, int count, List<String> tags, List<BlockchainConstants.MintStatus> mintStatus, String search);

    /**
     * Fetches a specific {@link BscToken} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param tokenIdOrName the profile ID
     * @return the {@link BscToken}, never null
     */
    BscToken getToken(String tokenIdOrName);

    /**
     * Updates the supplied {@link BscToken}.
     *
     * @param tokenId the id of the token to update
     * @param updateBscTokenRequest the update request for the token.
     * @return the {@link BscToken} as it was changed by the service.
     */
    BscToken updateToken(String tokenId, UpdateBscTokenRequest updateBscTokenRequest);

    /**
     * Updates the mint status of the supplied {@link BscToken}.
     *
     * @param tokenId the id of the token to update
     * @param status the mint status of the token
     * @return the {@link BscToken} as it was changed by the service.
     */
    BscToken setMintStatusForToken(String tokenId, BlockchainConstants.MintStatus status);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenRequest the {@link CreateBscTokenRequest} with the information to create
     * @return the {@link BscToken} as it was created by the service.
     */
    BscToken createToken(CreateBscTokenRequest tokenRequest);

    /**
     * Creates a new token by cloning an existing {@link BscToken} definition.
     *
     * @param bscToken the {@link BscToken} with the information to clone
     * @return the {@link BscToken} as it was created by the service.
     */
    BscToken cloneBscToken(BscToken bscToken);

    /**
     * Deletes the {@link BscToken} with the supplied profile ID.
     *
     * @param tokenId the bsc token ID.
     */
    void deleteToken(String tokenId);
}
