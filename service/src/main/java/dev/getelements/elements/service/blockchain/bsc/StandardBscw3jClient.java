package dev.getelements.elements.service.blockchain.bsc;

import dev.getelements.elements.Constants;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.crypto.CryptoException;
import dev.getelements.elements.model.blockchain.bsc.Web3jWallet;
import dev.getelements.elements.rt.util.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.primitive.Int;
import org.web3j.abi.datatypes.primitive.Long;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class StandardBscw3jClient implements Bscw3jClient {

    private static final Logger logger = LoggerFactory.getLogger(StandardBscw3jClient.class);

    private static final int IV_LENGTH = 16;

    private static final int SALT_LENGTH = 32;

    private static final int AES_KEY_LENGTH = 256;

    private static final int AES_ITERATIONS = 65536;

    private static final String AES = "AES";

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final String DEFAULT_PASSPHRASE = "dev.getelements.elements.model.blockchain.bsc.Web3jWallet";

    private static final SecureRandom sr = new SecureRandom();

    private HttpService httpService;

    private Web3j web3j;

    @Override
    public Web3jWallet createWallet(final String name, final String passphrase) {
        final var wallet = new Web3jWallet();
        wallet.setName(name);
        return generateKeyPair(wallet, passphrase);
    }

    @Override
    public Web3jWallet createWallet(final String name, final String passphrase, final String privateKey) {

        final var iv = new byte[IV_LENGTH];
        final var salt = new byte[SALT_LENGTH];
        sr.nextBytes(iv);
        sr.nextBytes(salt);

        final var credentials = Credentials.create(privateKey);
        final var encryptedCredentials = encrypt(iv, salt, credentials, passphrase);

        final var wallet = new Web3jWallet();
        wallet.setIv(Hex.encode(iv));
        wallet.setSalt(Hex.encode(salt));
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
                                    final String newPassphrase)  {

        final var updatedWallet = new Web3jWallet();
        updatedWallet.setName(name);
        updatedWallet.setExtra(wallet.getExtra());
        updatedWallet.setVersion(wallet.getVersion());

        if (wallet.getAccounts() != null && wallet.getAccounts().size() > 0) {

            final List<String> decrypted;

            final var iv = new byte[IV_LENGTH];
            final var salt = new byte[SALT_LENGTH];
            sr.nextBytes(iv);
            sr.nextBytes(salt);

            try {
                decrypted = wallet.getAccounts()
                    .stream()
                    .map(e -> decrypt(wallet, e, passphrase))
                    .collect(toList());
            } catch (CryptoException ex) {
                throw new InvalidDataException("Unable to decrypt one or more accounts.", ex);
            }

            final var accounts = decrypted
                .stream()
                .map(d -> encrypt(iv, salt, d, passphrase))
                .collect(toList());

            final var addresses = decrypted
                .stream()
                .map(Credentials::create)
                .map(Credentials::getAddress)
                .collect(toList());

            updatedWallet.setIv(Hex.encode(iv));
            updatedWallet.setSalt(Hex.encode(salt));
            updatedWallet.setAccounts(accounts);
            updatedWallet.setAddresses(addresses);

        } else {
            generateKeyPair(updatedWallet, newPassphrase);
        }

        return updatedWallet;

    }

    private Web3jWallet generateKeyPair(final Web3jWallet wallet, final String passphrase) {
        try{

            final var iv = new byte[IV_LENGTH];
            final var salt = new byte[SALT_LENGTH];
            sr.nextBytes(iv);
            sr.nextBytes(salt);

            final var ecKeyPair = Keys.createEcKeyPair();
            final var credentials = Credentials.create(ecKeyPair);
            final var encryptedCredentials = encrypt(iv, salt, credentials, passphrase);

            final var accounts = new ArrayList<String>();
            accounts.add(encryptedCredentials);

            final var addresses = new ArrayList<String>();
            addresses.add(credentials.getAddress());

            wallet.setIv(Hex.encode(iv));
            wallet.setSalt(Hex.encode(salt));
            wallet.setAccounts(accounts);
            wallet.setAddresses(addresses);

            return wallet;
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException ex) {
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

    public String encrypt(final byte[] iv, final byte[] salt, final String unencrypted, final String passphrase) {
        try {
            final var cipher = getCipher(iv, salt, passphrase, Cipher.ENCRYPT_MODE);
            final var unencryptedBytes = unencrypted.getBytes(StandardCharsets.UTF_8);
            final var encryptedBytes = cipher.doFinal(unencryptedBytes);
            final var encryptedString = Hex.encode(encryptedBytes);
            return encryptedString;
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            throw new CryptoException(ex);
        }
    }

    @Override
    public String decrypt(byte[] iv, byte[] salt, final String encrypted, final String passphrase) {
        try {
            final var cipher = getCipher(iv, salt, passphrase, Cipher.DECRYPT_MODE);
            final var encryptedBytes = Hex.decode(encrypted);
            final var decryptedBytes = cipher.doFinal(encryptedBytes);
            final var decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);
            return decryptedString;
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            throw new CryptoException(ex);
        }
    }

    private static char[] passphraseOrDefault(final String passphrase) {
        final var trimmed = passphrase == null ? "" : passphrase;
        return (trimmed.isBlank() ? DEFAULT_PASSPHRASE : trimmed).toCharArray();
    }

    private Cipher getCipher(final byte[] iv, final byte[] salt, final String passphrase, final int mode) {
        try {

            final var chars = passphraseOrDefault(passphrase);
            final var keySpec = new PBEKeySpec(chars, salt, AES_ITERATIONS, AES_KEY_LENGTH);
            final var factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
            final var secret = factory.generateSecret(keySpec);
            final var secretKeySpec = new SecretKeySpec(secret.getEncoded(), AES);
            final var ivParameterSpec = new IvParameterSpec(iv);
            final var cipher = Cipher.getInstance(AES_ALGORITHM);

            cipher.init(mode, secretKeySpec, ivParameterSpec);
            return cipher;
        } catch (
                NoSuchAlgorithmException |
                InvalidKeySpecException |
                InvalidKeyException |
                InvalidAlgorithmParameterException |
                NoSuchPaddingException ex) {
            throw new CryptoException(ex);
        }
    }

    @Override
    public Web3j getWeb3j() {
        return web3j;
    }

    @Inject
    private void setHttpService(@Named(Constants.BSC_RPC_PROVIDER)String bscHost) {
        httpService = new HttpService(bscHost);
        web3j = Web3j.build(httpService);
    }
}
