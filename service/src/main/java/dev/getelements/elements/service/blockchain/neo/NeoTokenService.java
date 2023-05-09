package dev.getelements.elements.service.blockchain.neo;

import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.neo.CreateNeoTokenRequest;
import dev.getelements.elements.model.blockchain.neo.NeoToken;
import dev.getelements.elements.model.blockchain.neo.UpdateNeoTokenRequest;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.Unscoped;

import java.util.List;

/**
 * Manages instances of {@link NeoToken}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.blockchain.neo.token"),
    @ModuleDefinition(
            value = "namazu.elements.service.blockchain.unscoped.neo.token",
            annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
public interface NeoTokenService {

    /**
     * Lists all {@link NeoToken} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param tags
     * @param mintStatus
     * @param search
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
     * @param tokenRequest the token information to update
     * @return the {@link NeoToken} as it was changed by the service.
     */
    NeoToken updateToken(String tokenId, UpdateNeoTokenRequest tokenRequest);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param tokenRequest the {@link CreateNeoTokenRequest} with the information to create
     * @return the {@link NeoToken} as it was created by the service.
     */
    NeoToken createToken(CreateNeoTokenRequest tokenRequest);

    /**
     * Deletes the {@link NeoToken} with the supplied token ID.
     *
     * @param tokenId the token ID.
     */
    void deleteToken(String tokenId);

}
