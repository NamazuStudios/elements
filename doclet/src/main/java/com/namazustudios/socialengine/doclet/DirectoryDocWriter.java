package com.namazustudios.socialengine.doclet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static java.nio.file.Files.createTempDirectory;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;

/**
 * Writes docs toa  directory
 */
public class DirectoryDocWriter implements DocWriter {

    private final Path root;

    private final int maxColumns;

    private final String indentation;

    private final String newline;

    private final String copyrightNotice;

    public DirectoryDocWriter(final Path root,
                              final int maxColumns,
                              final String indentation,
                              final String newline,
                              final String copyrightNotice) {
        this.root = root;
        this.maxColumns = maxColumns;
        this.indentation = indentation;
        this.newline = newline;
        this.copyrightNotice = copyrightNotice;
    }

    @Override
    public DocRootWriter open(final DocRoot docRoot) throws IOException {
        final var separator = root.getFileSystem().getSeparator();
        final var rootStream = stream(root.spliterator(), false).map(Path::toString);
        final var relativeStream = docRoot.getRelativePath().stream();
        final var filePathString = concat(rootStream, relativeStream).collect(joining(separator));
        final var filePath = Paths.get(filePathString);
        final var fos = new FileOutputStream(filePath.toFile());
        return new StreamDocRootWriter(fos, maxColumns, indentation, newline, copyrightNotice);
    }

    /**
     * Gets the root directory where this {@link DirectoryDocWriter} writes.
     *
     * @return the {@link Path}
     */
    public Path getRoot() {
        return root;
    }

    public static class Builder {

        private static final Pattern INDENTATION = Pattern.compile("\\s+");

        private Path root;

        private int maxColumns = 120;

        private String indentation = "    ";

        private String newline = "\n";

        private String copyrightNotice = "";

        public Builder withRoot(final Path root) {
            this.root = root;
            return this;
        }

        public Builder withMaxColumns(final int maxColumns) {
            if (maxColumns <= 0)
                throw new IllegalArgumentException("Max columns must be positive.");
            this.maxColumns = maxColumns;
            return this;
        }

        public Builder withTemporaryDirectory() throws IOException {
            this.root = createTempDirectory(DirectoryDocWriter.class.getSimpleName());
            return this;
        }

        public Builder withIndentation(final String indentation) {

            if (!INDENTATION.matcher(indentation).matches())
                throw new IllegalArgumentException("Invalid indentation: " + indentation);

            this.indentation = indentation;
            return this;
        }

        public Builder withNewline(final String newline) {
            this.newline = newline;
            return this;
        }

        public Builder withCopyrightNotice(final String copyrightNotice) {
            this.copyrightNotice = copyrightNotice.trim();
            return this;
        }

        public DirectoryDocWriter build() {
            return new DirectoryDocWriter(
                root,
                maxColumns,
                indentation,
                newline,
                copyrightNotice
            );
        }

    }

}
