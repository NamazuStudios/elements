package com.namazustudios.socialengine.service.blockchain;

import org.testng.annotations.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.testng.AssertJUnit.assertEquals;

public class StandardBscw3jClientTest {

    private final Bscw3jClient client = new StandardBscw3jClient();

    @Test
    public void testEncryptDecryptGenerate() {
        final var encrypted = client.createWallet("test", "asdf");
        final var encryptedAccount = encrypted.getAccounts().get(0);
        client.decrypt(encrypted, encryptedAccount, "asdf");
    }

    @Test
    public void testEncryptDecryptImport() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        final var ecKeyPair = Keys.createEcKeyPair();
        final var credentials = Credentials.create(ecKeyPair);
        final var privateKeyString = credentials.getEcKeyPair().getPrivateKey().toString(16);
        client.createWallet("test", "asdf", privateKeyString);
        final var encrypted = client.createWallet("test", "asdf");
        final var encryptedAccount = encrypted.getAccounts().get(0);
        final var decrypted = client.decrypt(encrypted, encryptedAccount, "asdf");
        assertEquals(decrypted, privateKeyString);
    }

}
