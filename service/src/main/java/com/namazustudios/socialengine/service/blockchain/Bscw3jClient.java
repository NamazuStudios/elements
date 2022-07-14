package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.blockchain.bsc.Web3jWallet;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;
import io.neow3j.types.ContractParameter;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.*;
import java.math.BigInteger;

/**
 * Manages instances of {@link Bscw3j}.
 *
 * Created by Tuan Tran on 3/24/21.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.blockchain.bsc.client"),
    @ModuleDefinition(
        value = "namazu.elements.service.blockchain.bsc.unscoped.client",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
public interface Bscw3jClient {

    /**
     * Gets the {@link Web3j} instance.
     * @return the {@link Web3j}, never null
     */
    Web3j getWeb3j();

    /**
     * Gets the {@link Web3jService} instance for making raw requests.
     * @return the {@link Web3jService}, never null
     */
    Web3jService getWeb3jService();

    /**
     * Gets the {@link Credentials} instance.
     *
     * @param wif the wallet id
     * @return the {@link Credentials}, never null
     */
    Credentials getAccount(String privateKey);

    /**
     * Gets the {@link Web3jWallet} instance.
     *
     * @param account the account of the wallet
     * @return the {@link Web3jWallet}, never null
     */
    Web3jWallet getWallet(BigInteger accountSecretKey);

    /**
     * Creates a {@link Web3jWallet}.
     *
     * @param name the name for the wallet
     * @return the {@link Web3jWallet}
     */
    Web3jWallet createWallet(String name) throws CipherException;

    /**
     * Creates an encrypted {@link Web3jWallet}.
     *
     * @param name the name for the wallet
     * @param password the password for the wallet
     * @return the {@link Web3jWallet}
     */
    Web3jWallet createWallet(String name, String password) throws CipherException;

    /**
     * Creates an encrypted {@link Web3jWallet}.
     *
     * @param name the name for the wallet
     * @param password the password for the wallet
     * @param privateKey the key for the account to be imported into this wallet
     * @return the {@link Web3jWallet}
     */
    Web3jWallet createWallet(String name, String password, String privateKey) throws CipherException;

    /**
     * Creates an encrypted {@link Web3jWallet}.
     *
     * @param wallet the NEP6Wallet
     * @param name the new name for the wallet
     * @param password the current password for the wallet
     * @param newPassword the new password for the wallet
     * @return the {@link Web3jWallet}
     */
    Web3jWallet updateWallet(Web3jWallet wallet, String name, String password, String newPassword) throws CipherException;

    /**
     * Constructs a {@link org.web3j.abi.datatypes.Type} representing the object.
     *
     * @param object the Java {@link Object}.
     * @return the {@link org.web3j.abi.datatypes.Type}, never null
     */
    org.web3j.abi.datatypes.Type convertObject(final Object object);
}

