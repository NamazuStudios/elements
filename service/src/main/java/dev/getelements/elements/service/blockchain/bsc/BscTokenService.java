package dev.getelements.elements.service.blockchain.bsc;

import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.bsc.CreateBscTokenRequest;
import dev.getelements.elements.model.blockchain.bsc.BscToken;
import dev.getelements.elements.model.blockchain.bsc.UpdateBscTokenRequest;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.Unscoped;

import java.util.List;

/**
 * Manages instances of {@link BscToken}.
 *
 * Created by TuanTran on 3/24/22.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.blockchain.bsc.token"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.blockchain.bsc.unscoped.token",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.bsc.token",
                deprecated = @DeprecationDefinition("Use eci.elements.service.blockchain.bsc.token instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.bsc.unscoped.token",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.blockchain.bsc.unscoped.token instead.")
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
