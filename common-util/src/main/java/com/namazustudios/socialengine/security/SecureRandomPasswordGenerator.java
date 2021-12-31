package com.namazustudios.socialengine.security;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.SecureRandom;

import static com.namazustudios.socialengine.Constants.GENERATED_PASSWORD_LENGTH;

public class SecureRandomPasswordGenerator implements PasswordGenerator {

    private static final String CANDIDATES =
        "abcdefghijklmnopqrstuvwxyz" +
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
        "1234567890!#$%^&*()){}";

    private int length;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {

        final int length = getLength();
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; ++i) {
            final int index = secureRandom.nextInt(CANDIDATES.length());
            stringBuilder.append(CANDIDATES.charAt(index));
        }

        return stringBuilder.toString();

    }

    public int getLength() {
        return length;
    }

    @Inject
    public void setLength(@Named(GENERATED_PASSWORD_LENGTH) int length) {
        this.length = length;
    }

}
