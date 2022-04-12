package com.namazustudios.socialengine.model.blockchain.bsc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.util.Base64;

public class Web3jWallet {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";

    private static final String WEB3J_KEY = "com.namazustudios.socialengine.model.blockchain.bsc.Web3jWallet";

    @ApiModelProperty("The name given to this wallet.")
    private String name;

    @ApiModelProperty("The version of this wallet.")
    private String version;

    @ApiModelProperty("The seed of this wallet.")
    private String seed;

    @ApiModelProperty("The accounts associated with this wallet.")
    private List<String> accounts;

    @ApiModelProperty("The extra object data associated with this wallet.")
    private Object extra;

    public Web3jWallet() {
    }

    public Web3jWallet(BigInteger accountSecretKey) {
        String sPrivatekeyInHex = accountSecretKey.toString(16);
        this.accounts = new ArrayList<>();
        this.accounts.add(sPrivatekeyInHex);
        this.name = sPrivatekeyInHex;
    }

    public Web3jWallet(String name) {
        this.name = name;
    }

    public Web3jWallet(String name, BigInteger accountSecretKey) {
        this.name = name;
        if (this.accounts == null) this.accounts = new ArrayList<>();
        this.accounts.add(accountSecretKey.toString(16));
    }

    public Web3jWallet(String name, BigInteger accountSecretKey, String password) {
        this.name = name;
        this.seed = Web3jWallet.encrypt(password);
        if (this.accounts == null) this.accounts = new ArrayList<>();
        this.accounts.add(accountSecretKey.toString(16));
    }

    public Web3jWallet(String name, String version, String password, BigInteger accountSecretKey, Object extra) {
        this.name = name;
        this.version = version;
        this.seed = Web3jWallet.encrypt(password);
        if (this.accounts == null) this.accounts = new ArrayList<>();
        this.accounts.add(accountSecretKey.toString(16));
        this.extra = extra;
    }

    public String getName() {
        return name;
    }

    public String getSeed() {
        return seed;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public Object getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Web3jWallet)) return false;
        Web3jWallet that = (Web3jWallet) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getSeed(), that.getSeed()) &&
                Objects.equals(getAccounts(), that.getAccounts()) &&
                Objects.equals(getExtra(), that.getExtra());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getVersion(), getVersion(), getAccounts(), getExtra());
    }

    @Override
    public String toString() {
        return "Web3jWallet{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", getSeed=" + getSeed() +
                ", accounts=" + accounts +
                ", extra=" + extra +
                '}';
    }

    public static String encrypt(String unencryptedString) {
        String encryptedString = null;
        try {
             byte[] arrayBytes = WEB3J_KEY.getBytes(UNICODE_FORMAT);;
             KeySpec ks = new DESedeKeySpec(arrayBytes);;
             SecretKeyFactory skf = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);;
             Cipher cipher  = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
             String myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;;
             SecretKey key = skf.generateSecret(ks);
             cipher.init(Cipher.ENCRYPT_MODE, key);
             byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
             byte[] encryptedText = cipher.doFinal(plainText);
             encryptedString = new String(Base64.getEncoder().encodeToString(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }


    public static String decrypt(String encryptedString) {
        String decryptedText=null;
        try {
            byte[] arrayBytes = WEB3J_KEY.getBytes(UNICODE_FORMAT);;
            KeySpec ks = new DESedeKeySpec(arrayBytes);;
            SecretKeyFactory skf = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);;
            Cipher cipher  = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
            String myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;;
            SecretKey key = skf.generateSecret(ks);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedText = Base64.getDecoder().decode(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText= new String(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedText;
    }
}
