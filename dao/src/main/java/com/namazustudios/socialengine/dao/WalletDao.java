package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.SmartContractTemplate;
import com.namazustudios.socialengine.model.blockchain.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.Wallet;

public interface WalletDao {

    /**
     * Lists all {@link Wallet} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param search
     * @return a {@link Pagination} of {@link Wallet} instances
     */
    Pagination<SmartContractTemplate> getWallets(int offset, int count, String search);

    /**
     * Fetches a specific {@link Wallet} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param walletIdOrName the profile ID
     * @return the {@link Wallet}, never null
     */
    Wallet getWallet(String walletIdOrName);

    /**
     * Updates the supplied {@link Wallet}.
     *
     * @param walletRequest the {@link UpdateWalletRequest} with the information to update
     * @return the {@link SmartContractTemplate} as it was changed by the service.
     */
    Wallet updateWallet(UpdateWalletRequest walletRequest);

    /**
     * Creates a new Wallet.
     *
     * @param walletRequest the {@link CreateWalletRequest} with the information to create
     * @return the {@link SmartContractTemplate} as it was created by the service.
     */
    Wallet createWallet(CreateWalletRequest walletRequest);

    /**
     * Deletes the {@link Wallet} with the supplied wallet ID.
     *
     * @param walletId the template ID.
     */
    void deleteWallet(String walletId);
}
