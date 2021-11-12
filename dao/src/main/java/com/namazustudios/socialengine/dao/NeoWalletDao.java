package com.namazustudios.socialengine.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.SmartContractTemplate;
import com.namazustudios.socialengine.model.blockchain.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import io.neow3j.wallet.nep6.NEP6Wallet;

import java.util.Optional;

public interface NeoWalletDao {

    /**
     * Lists all {@link NeoWallet} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param userId
     * @return a {@link Pagination} of {@link NeoWallet} instances
     */
    Pagination<NeoWallet> getWallets(int offset, int count, String userId);

    /**
     * Fetches a specific {@link NeoWallet} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletId the wallent name or ID to
     * @return the {@link NeoWallet}, never null
     */
    NeoWallet getWallet(String walletId);

    /**
     * Tries to fetch a users specific {@link NeoWallet} instance based on name. Returns null if specified named wallet is not found.
     *
     * @param userId the user ID to check for the wallet
     * @param walletName the wallet name
     * @return the {@link NeoWallet}
     */
    NeoWallet getWalletForUser(String userId, String walletName);

    /**
     * Updates the supplied {@link NeoWallet}.
     *
     * @param updatedWalletRequest the {@link UpdateWalletRequest} with the updated information
     * @return the {@link NeoWallet} as it was changed by the service.
     */
    NeoWallet updateWallet(UpdateWalletRequest updatedWalletRequest);

    /**
     * Creates a new Wallet.
     *
     * @param wallet the {@link NeoWallet} with the information to create
     * @return the {@link NeoWallet} as it was created by the service.
     */
    NeoWallet createWallet(NeoWallet wallet) throws JsonProcessingException;

    /**
     * Deletes the {@link NeoWallet} with the supplied wallet ID.
     *
     * @param walletId the template ID.
     */
    void deleteWallet(String walletId);
}
