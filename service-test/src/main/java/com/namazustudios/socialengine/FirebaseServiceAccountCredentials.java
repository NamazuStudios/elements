package com.namazustudios.socialengine;

import com.namazustudios.socialengine.exception.InternalException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FirebaseServiceAccountCredentials {

    public static String loadServiceAccountCredentials() {
        try (var is = FirebaseServiceAccountCredentials.class.getResourceAsStream("/service-account-credentials.json")) {
            final byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new InternalException(ex);
        }
    }

}
