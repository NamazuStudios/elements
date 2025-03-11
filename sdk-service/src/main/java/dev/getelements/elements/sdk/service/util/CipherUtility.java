package dev.getelements.elements.sdk.service.util;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.getelements.elements.rt.util.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;

/**
 * Deals with instances of {@link Cipher} as well as provides some standard utilities for encrypting data.
 */
public interface CipherUtility {

    /**
     * The standard encoding for encrypted strings.
     */
    Charset STANDARD_ENCODING = StandardCharsets.UTF_8;

    /**
     * Gets the {@link Cipher} instance used to operate
     *
     * @param privateKeyCrytpoAlgorithm
     * @return
     */
    Cipher getCipher(PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm);

    /**
     * Encrypts the given String and transforms it to the
     * @param privateKeyCrytpoAlgorithm the algorithm to use
     * @param publicKey the public key to use
     * @param input the input string
     * @return the encrypted string
     */
    default String encrypt(
            final PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm,
            final PublicKey publicKey,
            final String input) {

        final var cipher = getCipher(privateKeyCrytpoAlgorithm);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }

        final var unencrypted = input.getBytes(STANDARD_ENCODING);

        try {
            final var encrypted = cipher.doFinal(unencrypted);
            return Hex.encode(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new InternalException(e);
        }

    }

    /**
     * Decrypts the given string from hex format into decrypted format.
     *
     * @param privateKeyCrytpoAlgorithm the algorithm to use
     * @param privateKey the private key to use
     * @param input the input string
     *
     * @return the decrypted string
     */
    default Optional<String> decryptString(
            final PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm,
            final PrivateKey privateKey,
            final String input) {

        final var cipher = getCipher(privateKeyCrytpoAlgorithm);

        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }

        final var encrypted = Hex.decode(input);

        try {
            final var unencrypted = cipher.doFinal(encrypted);
            return Optional.of(new String(unencrypted, STANDARD_ENCODING));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return Optional.empty();
        }

    }

}
