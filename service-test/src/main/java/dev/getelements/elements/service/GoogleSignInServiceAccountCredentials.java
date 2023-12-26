package dev.getelements.elements.service;

import dev.getelements.elements.exception.InternalException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GoogleSignInServiceAccountCredentials {

    public static String loadServiceAccountCredentials() {
        try (var is = GoogleSignInServiceAccountCredentials.class.getResourceAsStream("/googlesignincredentials.json")) {
            final byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new InternalException(ex);
        }
    }

}
