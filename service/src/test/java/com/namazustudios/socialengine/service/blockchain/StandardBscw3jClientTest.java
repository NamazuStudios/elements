package com.namazustudios.socialengine.service.blockchain;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class StandardBscw3jClientTest {

    private final Bscw3jClient client = new StandardBscw3jClient();

    @Test
    public void testEncryptDecryptGenerate() {
        final var encrypted = client.createWallet("test", "asdf");
        final var encryptedAccount = encrypted.getAccounts().get(0);
        final var decrypted = client.decrypt(encrypted, "asdf", encryptedAccount);
        assertEquals(encryptedAccount, decrypted);
    }

}
