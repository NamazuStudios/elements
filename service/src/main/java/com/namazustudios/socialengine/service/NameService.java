package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

/**
 * Generates names based on the system configuration.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.unscoped.name", annotation = Unscoped.class)
})
public interface NameService {

    /**
     * Generates a randomly assigned name. The returned value is not guaranteed to be unique, as additional steps
     * need to be taken to ensure the name is unique.
     *
     * @return the randomly assigned name
     */
    String generateRandomName();

    /**
     * Generates a randomly-assigned qualifier string which is intended to be appended to the generated name. The
     * details of this are up to the implementation, but the default value is a four-digit random integer. Advanced
     * implementations may use something such as a database lookup or advanced random generator to generate a more
     * complete qualifier code.
     *
     * @return the generated qualifier code
     */
    default String generateQualifier() {
        final Random random = ThreadLocalRandom.current();
        return format("%04d", random.nextInt(9999));
    }

    /**
     * Generates a fully qualified name, which is the concatenation of {@link #generateRandomName()} and
     * {@link #generateQualifier()}
     *
     * @return
     */
    default String generateQualifiedName() {
        return format("%s%s", generateRandomName(), generateQualifier());
    }
}
