package dev.getelements.elements.service.codegen;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.service.codegen.CodegenService;

import java.io.File;

/**
 * Anonymous (and non-superuser) implementation of {@link CodegenService}.
 *
 * <p>Always throws {@link ForbiddenException}; superuser access is required for code generation.
 */
public class AnonCodegenService implements CodegenService {

    @Override
    public File generateCore(final File spec, final String language, final String packageName, final String options) {
        throw new ForbiddenException("You are not authorized to perform this operation");
    }

}
