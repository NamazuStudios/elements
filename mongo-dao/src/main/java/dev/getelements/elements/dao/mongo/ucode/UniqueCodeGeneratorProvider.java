package dev.getelements.elements.dao.mongo.ucode;

import dev.getelements.elements.sdk.util.OffensiveWordFilter;
import dev.getelements.elements.sdk.util.UniqueCodeGenerator;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.security.SecureRandom;

public class UniqueCodeGeneratorProvider implements Provider<UniqueCodeGenerator> {

    private Provider<SecureRandom> secureRandomProvider;

    private Provider<OffensiveWordFilter> offensiveWordFilterProvider;

    @Override
    public UniqueCodeGenerator get() {

        final var secureRandom = getSecureRandomProvider().get();
        final var offensiveWordFilter = getOffensiveWordFilterProvider().get();

        return new UniqueCodeGenerator.Builder()
                .withRandom(secureRandom)
                .rejectingOffensiveWords(offensiveWordFilter)
                .build();

    }

    public Provider<SecureRandom> getSecureRandomProvider() {
        return secureRandomProvider;
    }

    @Inject
    public void setSecureRandomProvider(Provider<SecureRandom> secureRandomProvider) {
        this.secureRandomProvider = secureRandomProvider;
    }

    public Provider<OffensiveWordFilter> getOffensiveWordFilterProvider() {
        return offensiveWordFilterProvider;
    }

    @Inject
    public void setOffensiveWordFilterProvider(Provider<OffensiveWordFilter> offensiveWordFilterProvider) {
        this.offensiveWordFilterProvider = offensiveWordFilterProvider;
    }

}
