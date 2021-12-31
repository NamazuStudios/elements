package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

/**
 * Generates names based on the system configuration.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.name"),
    @ExposedModuleDefinition(
        value = "namazu.elements.service.unscoped.name",
        annotation = @ExposedBindingAnnotation(Unscoped.class))
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
     * details of this are up to the implementation, but the default value is a nine-digit random integer. Advanced
     * implementations may use something such as a database lookup or advanced random generator to generate a more
     * complete qualifier code.
     *
     * @return the generated qualifier code
     */
    default String generateQualifier() {
        return generateQualifier(999999999);
    }

    /**
     * Generates a randomly-assigned qualifier string which is intended to be appended to the generated name. The
     * details of this are up to the implementation, but the default value is a padded integer up to the specified
     * maximum value.
     *
     * @return the generated qualifier code
     */
    default String generateQualifier(final int max) {
        final var random = ThreadLocalRandom.current();
        final var digits = Integer.toString(max).length();
        return format("%0" + digits + "d", random.nextInt(max));
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

    /**
     * Generates an email address for an anonymous user.
     *
     * @param name the username part of the email
     * @return a fully formatted anonymous email
     */
    default String formatAnonymousEmail(final String name) {
        return format("%s@anonymous.invalid", name);
    }

    /**
     * Assigns a generated name and email to the supplied user, if either field is null.
     *
     * @param user the user
     * @return the user as it was updated
     */
    default User assignNameAndEmailIfNecessary(final User user) {

        if (user.getName() == null || user.getEmail() == null) {
            final var name = generateQualifiedName();
            if (user.getName() == null) user.setName(name);
            if (user.getEmail() == null) user.setEmail(formatAnonymousEmail(name));
        }

        return user;

    }

}
