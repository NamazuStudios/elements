package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscTokenRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscToken;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscTokenRequest;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

import java.util.List;

/**
 * Manages instances of {@link BscToken}.
 *
 * Created by TuanTran on 3/24/22.
 */
@Expose({
        @ExposedModuleDefinition(value = "namazu.elements.service.blockchain.bsc.token"),
        @ExposedModuleDefinition(
                value = "namazu.elements.service.blockchain.bsc.unscoped.token",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface BscTokenService {

    /**
     * Lists all {@link BscToken} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param tags
     * @param mintStatus
     * @param search
     * @return a {@link Pagination} of {@link BscToken} instances
     */
    Pagination<BscToken> getTokens(int offset, int count, List<String> tags, BlockchainConstants.MintStatus mintStatus, String search);

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
     * @param tokenRequest the token information to update
     * @return the {@link BscToken} as it was changed by the service.
     */
    BscToken updateToken(String tokenId, UpdateBscTokenRequest tokenRequest);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenRequest the {@link CreateBscTokenRequest} with the information to create
     * @return the {@link BscToken} as it was created by the service.
     */
    BscToken createToken(CreateBscTokenRequest tokenRequest);

    /**
     * Deletes the {@link BscToken} with the supplied token ID.
     *
     * @param tokenId the token ID.
     */
    void deleteToken(String tokenId);

}
