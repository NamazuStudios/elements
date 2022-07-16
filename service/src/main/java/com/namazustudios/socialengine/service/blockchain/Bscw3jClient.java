package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.blockchain.bsc.Web3jWallet;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

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
     * Creates an encrypted {@link Web3jWallet}.
     *
     * @param name the name for the wallet
     * @param password the password for the wallet
     * @return the {@link Web3jWallet}
     */
    Web3jWallet createWallet(String name, String password);

    /**
     * Creates an encrypted {@link Web3jWallet}.
     *
     * @param name the name for the wallet
     * @param password the password for the wallet
     * @param privateKey the key for the account to be imported into this wallet
     * @return the {@link Web3jWallet}
     */
    Web3jWallet createWallet(String name, String password, String privateKey);

    /**
     * Creates an encrypted {@link Web3jWallet}.
     *
     * @param wallet the NEP6Wallet
     * @param name the new name for the wallet
     * @param password the current password for the wallet
     * @param newPassword the new password for the wallet
     * @return the {@link Web3jWallet}
     */
    Web3jWallet updateWallet(Web3jWallet wallet, String name, String password, String newPassword);

    /**
     * Constructs a {@link org.web3j.abi.datatypes.Type} representing the object.
     *
     * @param object the Java {@link Object}.
     * @return the {@link org.web3j.abi.datatypes.Type}, never null
     */
    org.web3j.abi.datatypes.Type<?> convertObject(final Object object);

    /**
     * Encrypts the credentials with the iv, salt, passphrase, and raw credentials.
     *
     * @param iv
     * @param salt
     * @param passphrase
     * @param credentials
     * @return
     */
    default String encrypt(byte[] iv, byte[] salt, String passphrase, Credentials credentials) {
        final var credentialsString = credentials.getEcKeyPair().getPrivateKey().toString(16);
        return encrypt(iv, salt, passphrase, credentialsString);
    }

    /**
     * Encrypts the credentials with the iv, salt, passphrase, and raw credentials.
     *
     * @param iv
     * @param salt
     * @param passphrase
     * @param credentials
     * @return
     */
    String  encrypt(byte[] iv, byte[] salt, String passphrase, String credentials);


    String decrypt(final Web3jWallet wallet, String encryptedString);

    String decrypt(final Web3jWallet wallet, String passphrase, String encryptedString);

    String decrypt(byte[] iv, byte[] salt, String passphrase, String encryptedString);

}

