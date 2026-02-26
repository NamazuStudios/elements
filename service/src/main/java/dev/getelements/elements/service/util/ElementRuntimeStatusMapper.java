package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.model.system.ElementRuntimeStatus;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.mapstruct.Mapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

@Mapper(uses = {ElementMetadataMapper.class})
public interface ElementRuntimeStatusMapper extends MapperRegistry.Mapper<RuntimeRecord, ElementRuntimeStatus> {

    @Override
    ElementRuntimeStatus forward(RuntimeRecord source);

    /**
     * Strips off the temporary path prefix and returns the path relative to the global temporary path root.
     *
     * @param path the path
     * @return the path as a string
     */
    static String pathToString(final Path path) {

        if (path == null) {
            return null;
        }

        if (TemporaryFiles.isTemporaryPath(path)) {
            return TemporaryFiles.getTemporaryRoot().relativize(path).toString();
        }

        return "%s@%s".formatted(path.getFileSystem(), path);

    }

    /**
     * Converts the exception to a string by printing its stacktrace to the string.
     *
     * @param throwable the throwable
     * @return a string from the throwable
     */
    static String exceptionToString(final Throwable throwable) {

        if (throwable == null) {
            return null;
        }

        final var writer = new StringWriter();
        final var printer = new PrintWriter(writer);
        throwable.printStackTrace(printer);
        return writer.toString();

    }

}