package com.namazustudios.socialengine.dao;

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
     * Fetches a specific {@link NeoWallet} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param walletIdOrName the profile ID
     * @return the {@link NeoWallet}, never null
     */
    Optional<NeoWallet> getWallet(String walletIdOrName);

    /**
     * Updates the supplied {@link NeoWallet}.
     *
     * @param wallet the {@link UpdateWalletRequest} with the information to update
     * @return the {@link SmartContractTemplate} as it was changed by the service.
     */
    NeoWallet updateWallet(UpdateWalletRequest wallet);

    /**
     * Creates a new Wallet.
     *
     * @param wallet the {@link NEP6Wallet} with the information to create
     * @return the {@link NeoWallet} as it was created by the service.
     */
    NeoWallet createWallet(NeoWallet wallet);

    /**
     * Deletes the {@link NeoWallet} with the supplied wallet ID.
     *
     * @param walletId the template ID.
     */
    void deleteWallet(String walletId);
}
