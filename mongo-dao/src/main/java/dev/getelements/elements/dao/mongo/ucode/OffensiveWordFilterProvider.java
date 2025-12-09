package dev.getelements.elements.dao.mongo.ucode;

import dev.getelements.elements.sdk.util.OffensiveWordFilter;
import jakarta.inject.Provider;

public class OffensiveWordFilterProvider implements Provider<OffensiveWordFilter> {

    @Override
    public OffensiveWordFilter get() {
        return new OffensiveWordFilter.Builder()
                .ignoringCase()
                .addDefaultWords()
                .build();
    }

}
