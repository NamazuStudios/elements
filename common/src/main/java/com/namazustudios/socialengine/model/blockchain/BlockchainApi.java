package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;

import java.util.Set;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

/**
 * Enumerates the various blockchain protocols supported by elements.
 */
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public enum BlockchainApi {

    /**
     * The Neo Protocol.
     */
    NEO(ELEMENTS_JSON_RPC_PROTOCOL),

    /**
     * The Ethereum Protocol.
     */
    ETHEREUM(ELEMENTS_JSON_RPC_PROTOCOL),

    /**
     * The Solana protocol.
     */
    SOLANA(ELEMENTS_JSON_RPC_PROTOCOL);

    /**
     * Lists all protocols supported by this {@link BlockchainApi}.
     */
    private final Set<String> protocols;

    BlockchainApi(final String ... protocols) {
        this.protocols = Set.of(protocols);
    }

    /**
     * Gets the protocols, as specified in the {@link RemoteScope#protocol()} annotation.
     *
     * @return the protocols supported by this API
     */
    public Set<String> getProtocols() {
        return protocols;
    }

}
