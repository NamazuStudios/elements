package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.nep6.NEP6Wallet;

/**
 * Manages instances of {@link Neow3j}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ExposedModuleDefinition(value = "namazu.elements.service.blockchain.neow3j"),
        @ExposedModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.neow3j",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface Neow3jService {

    /**
     * Gets the {@link Neow3j} instance.
     * @return the {@link Neow3j}, never null
     */
    Neow3j getNeow3j();

    /**
     * Gets the {@link Account} instance.
     *
     * @param wif the wallet id
     * @return the {@link Account}, never null
     */
    Account getAccount(String wif);

    /**
     * Gets the {@link NEP6Wallet} instance.
     *
     * @param account the account of the wallet
     * @return the {@link NEP6Wallet}, never null
     */
    NEP6Wallet getWallet(Account account);

    /**
     * Creates a {@link NEP6Wallet}.
     *
     * @param name the name for the wallet
     * @return the {@link NEP6Wallet}
     */
    NEP6Wallet createWallet(String name) throws CipherException;

    /**
     * Creates an encrypted {@link NEP6Wallet}.
     *
     * @param name the name for the wallet
     * @param password the password for the wallet
     * @return the {@link NEP6Wallet}
     */
    NEP6Wallet createWallet(String name, String password) throws CipherException;

    /**
     * Creates an encrypted {@link NEP6Wallet}.
     *
     * @param wallet the NEP6Wallet
     * @param name the new name for the wallet
     * @param password the current password for the wallet
     * @param newPassword the new password for the wallet
     * @return the {@link NEP6Wallet}
     */
    NEP6Wallet updateWallet(NEP6Wallet wallet, String name, String password, String newPassword) throws CipherException, NEP2InvalidPassphrase, NEP2InvalidFormat;

    /**
     * Gets the {@link ScriptBuilder} instance.
     * @return the {@link ScriptBuilder}, never null
     */
    ScriptBuilder getScriptBuilder();

    /**
     * Gets a new {@link TransactionBuilder}.
     *
     * @param neow3j the neow3j connection.
     * @return the {@link TransactionBuilder}, never null
     */
    TransactionBuilder getTransactionBuilder(Neow3j neow3j);

}

