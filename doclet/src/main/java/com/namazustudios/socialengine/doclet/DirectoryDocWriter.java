package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.util.TemporaryFiles;

import javax.tools.Diagnostic;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;

/**
 * Writes docs toa  directory
 */
public class DirectoryDocWriter implements DocWriter {

    private static final TemporaryFiles tempFiles = new TemporaryFiles(DirectoryDocWriter.class);

    private final Path root;

    private final int maxColumns;

    private final String indentation;

    private final String newline;

    private final String copyrightNotice;

    private final DocContext docContext;

    public DirectoryDocWriter(final Path root,
                              final int maxColumns,
                              final String indentation,
                              final String newline,
                              final String copyrightNotice,
                              final DocContext docContext) {
        this.root = root.toAbsolutePath();
        this.maxColumns = maxColumns;
        this.indentation = indentation;
        this.newline = newline;
        this.copyrightNotice = copyrightNotice;
        this.docContext = docContext;
    }

    @Override
    public void reset() throws IOException {
        walkFileTree(getRoot(), new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                if (!getRoot().equals(dir)) delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    @Override
    public DocRootWriter open(final DocRoot docRoot) throws IOException {

        // Builds the Path
        final var separator = getRoot().getFileSystem().getSeparator();
        final var rootStream = stream(getRoot().spliterator(), false).map(Path::toString);
        final var relativeStream = docRoot.getRelativePath().stream();
        final var filePathString = separator + concat(rootStream, relativeStream).collect(joining(separator));
        final var filePath = Paths.get(filePathString);

        createDirectories(filePath.getParent());
        docContext.getReporter().print(Diagnostic.Kind.NOTE, "Generating: " + filePath.toString());

        // Creates the writer
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

        private Path root;

        private int maxColumns = 120;

        private String indentation = "    ";

        private String newline = "\n";

        private String copyrightNotice = "";

        private DocContext docContext = null;

        private List<String> authors = new ArrayList<>();

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
            this.root = tempFiles.createTempDirectory();
            return this;
        }

        public Builder withIndentation(final String indentation) {
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

        public Builder withDocContext(final DocContext docContext) {
            this.docContext = docContext;
            return this;
        }

        public Builder withAuthor(final String author) {
            this.authors.add(author);
            return this;
        }

        public DirectoryDocWriter build() {

            if (root == null) throw new IllegalStateException("Directory root not specified.");
            if (docContext == null) throw new IllegalStateException("DocContext not specified.");

            return new DirectoryDocWriter(
                root,
                maxColumns,
                indentation,
                newline,
                copyrightNotice,
                docContext
            );

        }

    }

}
