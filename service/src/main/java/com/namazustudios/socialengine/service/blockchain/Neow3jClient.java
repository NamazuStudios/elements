package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.blockchain.neo.Nep6Wallet;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.nep6.NEP6Wallet;

/**
 * Manages instances of {@link Neow3j}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.blockchain.neo.client"),
    @ModuleDefinition(
        value = "namazu.elements.service.blockchain.unscoped.neo.client",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
public interface Neow3jClient {

    /**
     * Gets the {@link Neow3j} instance.
     * @return the {@link Neow3j}, never null
     */
    Neow3j getNeow3j();

    /**
     * Gets the {@link Neow3jService} instance for making raw requests.
     * @return the {@link Neow3jService}, never null
     */
    Neow3jService getNeow3jService();

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
     * @param name the name for the wallet
     * @param password the password for the wallet
     * @param privateKey the key for the account to be imported into this wallet
     * @return the {@link NEP6Wallet}
     * @throws CipherException
     */
    NEP6Wallet createWallet(String name, String password, String privateKey) throws CipherException;

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

    /**
     * Converts a {@link Nep6Wallet} into a {@link NEP6Wallet}.
     *
     * @param wallet the elements {@link Nep6Wallet}.
     * @return the converted {@link NEP6Wallet}, never null
     */
    NEP6Wallet elementsWalletToNEP6(Nep6Wallet wallet);

    /**
     * Converts a {@link NEP6Wallet} into a {@link Nep6Wallet}.
     *
     * @param wallet the {@link NEP6Wallet}.
     * @return the converted {@link Nep6Wallet}, never null
     */
    Nep6Wallet nep6ToElementsWallet(NEP6Wallet wallet);

    /**
     * Constructs a {@link SmartContract} representing the smart contract with the given script hash.
     *
     * @param hash the {@link Hash160} as a string.
     * @return the {@link SmartContract}, never null
     */
    SmartContract getSmartContract(String hash);

    /**
     * Constructs a {@link ContractParameter} representing the object.
     *
     * @param object the Java {@link Object}.
     * @return the {@link ContractParameter}, never null
     */
    ContractParameter convertObject(final Object object);
}

