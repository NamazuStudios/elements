package dev.getelements.elements.service.blockchain;

import dev.getelements.elements.exception.crypto.CryptoException;
import dev.getelements.elements.service.blockchain.bsc.Bscw3jClient;
import dev.getelements.elements.service.blockchain.bsc.StandardBscw3jClient;
import org.testng.annotations.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

public class StandardBscw3jClientTest {

    private final Bscw3jClient client = new StandardBscw3jClient();

    @Test
    public void testEncryptDecryptGenerate() {

        final var encrypted = client.createWallet("test", "asdf");
        assertEquals(1, encrypted.getAccounts().size());
        assertEquals(1, encrypted.getAddresses().size());

        final var encryptedAccount = encrypted.getAccounts().get(0);
        client.decrypt(encrypted, encryptedAccount, "asdf");

        try {
            client.decrypt(encrypted, encryptedAccount, "bogo");
            fail("Expected exception.");
        } catch (CryptoException ex) {
            // Test Passes
        }

    }

    @Test
    public void testEncryptDecryptImport() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {

        final var ecKeyPair = Keys.createEcKeyPair();
        final var credentials = Credentials.create(ecKeyPair);
        final var privateKeyString = credentials.getEcKeyPair().getPrivateKey().toString(16);
        final var encrypted = client.createWallet("test", "asdf", privateKeyString);
        assertEquals(1, encrypted.getAccounts().size());
        assertEquals(1, encrypted.getAddresses().size());

        final var encryptedAccount = encrypted.getAccounts().get(0);
        final var decrypted = client.decrypt(encrypted, encryptedAccount, "asdf");
        assertEquals(decrypted, privateKeyString);

        try {
            client.decrypt(encrypted, encryptedAccount, "bogo");
            fail("Expected exception.");
        } catch (CryptoException ex) {
            // Test Passes
        }

    }

    @Test
    public void testUpdateWallet() {

        final var encrypted = client.createWallet("test", "asdf");
        assertEquals(1, encrypted.getAccounts().size());
        assertEquals(1, encrypted.getAddresses().size());

        final var updated = client.updateWallet(encrypted, "test_a", "asdf", "fdsa");

        assertEquals(1, encrypted.getAccounts().size());
        assertEquals(1, encrypted.getAddresses().size());

        assertEquals("test_a", updated.getName());
        assertEquals(encrypted.getAddresses(), updated.getAddresses());
        assertNotEquals(encrypted.getIv(), updated.getIv());
        assertNotEquals(encrypted.getIv(), updated.getSalt());
        assertNotEquals(encrypted.getAccounts(), updated.getAccounts());

        try {
            final var encryptedAccount = updated.getAccounts().get(0);
            client.decrypt(encrypted, encryptedAccount, "bogo");
            fail("Expected exception.");
        } catch (CryptoException ex) {
            // Test Passes
        }

    }

    @Test
    public void testUpdateWalletImport() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {

        final var ecKeyPair = Keys.createEcKeyPair();
        final var credentials = Credentials.create(ecKeyPair);
        final var privateKeyString = credentials.getEcKeyPair().getPrivateKey().toString(16);
        final var encrypted = client.createWallet("test", "asdf", privateKeyString);
        assertEquals(1, encrypted.getAccounts().size());
        assertEquals(1, encrypted.getAddresses().size());

        final var updated = client.updateWallet(encrypted, "test_a", "asdf", "fdsa");

        assertEquals(1, encrypted.getAccounts().size());
        assertEquals(1, encrypted.getAddresses().size());

        assertEquals("test_a", updated.getName());
        assertEquals(encrypted.getAddresses(), updated.getAddresses());
        assertNotEquals(encrypted.getIv(), updated.getIv());
        assertNotEquals(encrypted.getIv(), updated.getSalt());
        assertNotEquals(encrypted.getAccounts(), updated.getAccounts());

        try {
            final var encryptedAccount = updated.getAccounts().get(0);
            client.decrypt(encrypted, encryptedAccount, "bogo");
            fail("Expected exception.");
        } catch (CryptoException ex) {
            // Test Passes
        }

    }

}
