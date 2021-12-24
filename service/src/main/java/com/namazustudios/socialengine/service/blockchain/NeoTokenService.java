package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateNeoTokenRequest;
import com.namazustudios.socialengine.model.blockchain.NeoToken;
import com.namazustudios.socialengine.model.blockchain.UpdateNeoTokenRequest;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

import java.util.List;

/**
 * Manages instances of {@link NeoToken}.
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
public interface NeoTokenService {

    /**
     * Lists all {@link NeoToken} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param tags
     * @param search
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
