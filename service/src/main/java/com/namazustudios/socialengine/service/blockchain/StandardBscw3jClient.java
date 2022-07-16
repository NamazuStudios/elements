package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.crypto.CryptoException;
import com.namazustudios.socialengine.model.blockchain.bsc.Web3jWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.primitive.Int;
import org.web3j.abi.datatypes.primitive.Long;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class StandardBscw3jClient implements Bscw3jClient {

    private static final Logger logger = LoggerFactory.getLogger(StandardBscw3jClient.class);

    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";

    private static final String DEFAULT_PASSPHRASE = "com.namazustudios.socialengine.model.blockchain.bsc.Web3jWallet";

    private HttpService httpService;

    @Override
    public Web3j getWeb3j() {
        return Web3j.build(httpService);
    }

    @Override
    public Web3jWallet createWallet(final String name) {
        return createWallet(name, DEFAULT_PASSPHRASE);
    }

    @Override
    public Web3jWallet createWallet(final String name, final String passphrase) {
        final var wallet = new Web3jWallet();
        wallet.setName(name);
        return generateKeyPair(wallet, passphrase);
    }

    @Override
    public Web3jWallet createWallet(final String name, final String passphrase, final String privateKey) {

        final var credentials = Credentials.create(privateKey);
        final var encryptedCredentials = encrypt(credentials);

        final var wallet = new Web3jWallet();
        wallet.setName(name);

        final var accounts = new ArrayList<String>();
        accounts.add(encryptedCredentials);

        final var addresses = new ArrayList<String>();
        addresses.add(credentials.getAddress());

        wallet.setAccounts(accounts);
        wallet.setAddresses(addresses);

        return wallet;

    }

    @Override
    public Web3jWallet updateWallet(final Web3jWallet wallet,
                                    final String name,
                                    final String passphrase,
                                    final String newPassphrase) throws CipherException {

        final var updatedWallet = new Web3jWallet();
        updatedWallet.setName(name);
        updatedWallet.setExtra(wallet.getExtra());
        updatedWallet.setVersion(wallet.getVersion());

        if (wallet.getAccounts() != null && wallet.getAccounts().size() > 0) {

            final List<String> decrypted;

            try {
                decrypted = wallet.getAccounts()
                    .stream()
                    .map(e -> decrypt(e, passphrase))
                    .collect(toList());
            } catch (CryptoException ex) {
                throw new InvalidDataException("Unable to decrypt one or more accounts.");
            }

            final var accounts = decrypted
                .stream()
                .map(d -> encrypt(d, newPassphrase))
                .collect(toList());

            final var addresses = decrypted
                .stream()
                .map(Credentials::create)
                .map(Credentials::getAddress)
                .collect(toList());

            updatedWallet.setAccounts(accounts);
            updatedWallet.setAddresses(addresses);

        } else {
            generateKeyPair(updatedWallet, newPassphrase);
        }

        return updatedWallet;

    }

    private Web3jWallet generateKeyPair(final Web3jWallet wallet, final String passphrase) {
        try{

            final var ecKeyPair = Keys.createEcKeyPair();
            final var credentials = Credentials.create(ecKeyPair);
            final var encryptedCredentials = encrypt(credentials, passphrase);

            final var accounts = new ArrayList<String>();
            accounts.add(encryptedCredentials);

            final var addresses = new ArrayList<String>();
            addresses.add(credentials.getAddress());

            wallet.setAccounts(accounts);
            wallet.setAddresses(addresses);

            return wallet;
        }catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new CryptoException(ex);
        }
    }

    @Override
    public Type convertObject(final Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof String) {
            final var str = (String)object;
            if(Numeric.containsHexPrefix(str))
                return new Address(str);
            return new Utf8String(str);
        } else if (object instanceof Integer) {
            return new Int((Integer)object);
        } else if (object instanceof Long) {
            return new Long((long) object);
        } else if (object instanceof Boolean) {
            return new Bool((Boolean) object);
        } else if (object instanceof List) {
            return convertList((List) object);
        } else {
            throw new IllegalArgumentException("Invalid object: " + object);
        }
    }

    private DynamicArray convertList(final List<Object> list) {
        final var l = list.stream()
                .map(this::convertObject)
                .collect(toList());

        Type prev = null;
        for (Type t : l) {
            if(prev != null && !Objects.equals(t.getTypeAsString(), prev.getTypeAsString())) {
                return new DynamicStruct(l);
            }
            prev = t;
        }

        return new DynamicArray(l);
    }

    @Inject
    private void setHttpService(@Named(Constants.BSC_RPC_PROVIDER)String bscHost) {
        httpService = new HttpService(bscHost);
    }

    @Override
    public String encrypt(final Credentials credentials) {
        final var credentialsString = credentials.getEcKeyPair().getPrivateKey().toString(16);
        return encrypt(credentialsString, DEFAULT_PASSPHRASE);
    }

    @Override
    public String encrypt(final Credentials credentials, final String passphrase) {
        final var credentialsString = credentials.getEcKeyPair().getPrivateKey().toString(16);
        return encrypt(credentialsString, passphrase);
    }

    @Override
    public String encrypt(final String unencryptedString, final String passphrase) {
        try {
            final var passphraseBytes = passphraseOrDefault(passphrase);
            KeySpec ks = new DESedeKeySpec(passphraseBytes);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);
            Cipher cipher  = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
            SecretKey key = skf.generateSecret(ks);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = unencryptedString.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedText = cipher.doFinal(plainText);
            return Base64.getEncoder().encodeToString(encryptedText);
        } catch (
                InvalidKeySpecException |
                NoSuchAlgorithmException |
                IllegalBlockSizeException |
                BadPaddingException |
                NoSuchPaddingException |
                InvalidKeyException ex) {
            throw new CryptoException(ex);
        }
    }

    @Override
    public String decrypt(final String encryptedString) {
        return decrypt(encryptedString, DEFAULT_PASSPHRASE);
    }

    @Override
    public String decrypt(final String encryptedString, final String passphrase) {
        try {
            final var passphraseBytes = passphraseOrDefault(passphrase);
            KeySpec ks = new DESedeKeySpec(passphraseBytes);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);
            Cipher cipher  = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
            SecretKey key = skf.generateSecret(ks);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedText = Base64.getDecoder().decode(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            return new String(plainText);
        } catch (
                InvalidKeySpecException |
                        NoSuchAlgorithmException |
                        IllegalBlockSizeException |
                        BadPaddingException |
                        NoSuchPaddingException |
                        InvalidKeyException ex) {
            throw new CryptoException(ex);
        }
    }

    private static byte[] passphraseOrDefault(final String passphrase) {
        final var trimmed = passphrase == null ? "" : passphrase;
        return (trimmed.isBlank() ? DEFAULT_PASSPHRASE : trimmed).getBytes(StandardCharsets.UTF_8);
    }

}
