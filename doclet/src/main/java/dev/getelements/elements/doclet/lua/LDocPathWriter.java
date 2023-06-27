package dev.getelements.elements.doclet.lua;

import dev.getelements.elements.doclet.DocRoot;
import dev.getelements.elements.doclet.DocRootWriter;
import dev.getelements.elements.doclet.DocWriter;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.*;
import static java.util.stream.Collectors.joining;

public class LDocPathWriter implements DocWriter {

    private final Path directory;

    public LDocPathWriter(final Path directory) throws IOException {
        if (!exists(directory)) createDirectories(directory);
        else if (!isDirectory(directory)) throw new IOException(directory + " exists, but is not a directory.");
        this.directory = directory.normalize().toAbsolutePath();
    }

    @Override
    public DocRootWriter open(final DocRoot docRoot) throws IOException {

        final var relative = docRoot.getRelativePath();

        final var absolute = directory.resolve(relative
            .stream()
            .collect(joining(directory.getFileSystem().getSeparator()))
        );

        return null;

    }

}
