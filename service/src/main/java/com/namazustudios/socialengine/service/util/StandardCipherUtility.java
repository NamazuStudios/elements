package com.namazustudios.socialengine.service.util;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

public class StandardCipherUtility implements CipherUtility {

    @Override
    public Cipher getCipher(PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm) {
        switch (privateKeyCrytpoAlgorithm) {
            case RSA_256:
            case RSA_384:
            case RSA_512:
                try {
                    return Cipher.getInstance(CryptoKeyPairUtility.RSA_ALGO);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
                    throw new InternalException(ex);
                }
            case ECDSA_256:
            case ECDSA_384:
            case ECDSA_512:
                try {
                    return Cipher.getInstance(CryptoKeyPairUtility.ECDSA_ALGO);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
                    throw new InternalException(ex);
                }
            default:
                // This should never happen
                throw new IllegalArgumentException("Unsupported algorithm: " + privateKeyCrytpoAlgorithm);
        }
    }

}
