package dev.getelements.elements.dao;

import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.neo.CreateNeoTokenRequest;
import dev.getelements.elements.model.blockchain.neo.NeoToken;
import dev.getelements.elements.model.blockchain.neo.UpdateNeoTokenRequest;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.util.List;

/**
 * Created by garrettmcspadden on 11/23/21.
 */
@Expose({
        @ModuleDefinition("eci.elements.dao.neo.token"),
        @ModuleDefinition(
                value = "namazu.elements.dao.neo.token",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.neo.token instead")
        ),
        @ModuleDefinition(
                value = "namazu.socialengine.dao.neo.token",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.neo.token instead")
        )
})
public interface NeoTokenDao {

    /**
     * Lists all {@link NeoToken} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param tags
     * @param mintStatus
     * @param search - name or type
     * @return a {@link Pagination} of {@link NeoToken} instances
     */
    Pagination<NeoToken> getTokens(int offset, int count, List<String> tags, List<BlockchainConstants.MintStatus> mintStatus, String search);

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
     * Updates the mint status of the supplied {@link NeoToken}.
     *
     * @param tokenId the id of the token to update
     * @param status the mint status of the token
     * @return the {@link NeoToken} as it was changed by the service.
     */
    NeoToken setMintStatusForToken(String tokenId, BlockchainConstants.MintStatus status);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenRequest the {@link CreateNeoTokenRequest} with the information to create
     * @return the {@link NeoToken} as it was created by the service.
     */
    NeoToken createToken(CreateNeoTokenRequest tokenRequest);

    /**
     * Creates a new token by cloning an existing {@link NeoToken} definition.
     *
     * @param neoToken the {@link NeoToken} with the information to clone
     * @return the {@link NeoToken} as it was created by the service.
     */
    NeoToken cloneNeoToken(NeoToken neoToken);

    /**
     * Deletes the {@link NeoToken} with the supplied profile ID.
     *
     * @param tokenId the neo token ID.
     */
    void deleteToken(String tokenId);

}
