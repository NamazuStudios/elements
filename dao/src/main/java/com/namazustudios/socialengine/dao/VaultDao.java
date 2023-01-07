package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.blockchain.VaultNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;

import java.util.Optional;

public interface VaultDao {

    Pagination<Vault> getVaults(int offset, int count, String userId);

    void deleteVault(String vaultId);

    Optional<Vault> findVault(String vaultId);

    default Vault getVault(String vaultId) {
        return findVault(vaultId).orElseThrow(VaultNotFoundException::new);
    }

    Vault createVault(Vault vault);

    Vault updateVault(Vault vault);

}
