package com.namazustudios.socialengine.doclet;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;

import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

/**
 * An implementation of {@link DocRootWriter} which is backed by an instance of {@link PrintStream}.
 */
public class StreamDocRootWriter implements DocRootWriter {

    private final int maxColumns;

    private final String indentation;

    private final String newline;

    private final PrintStream stream;

    private final String copyrightNotice;

    private final Deque<Indentation> indentations = new ArrayDeque<>();

    /**
     * Creates a {@link DocRootWriter} from the instance of {@link OutputStream}.
     *
     * @param stream the {@link OutputStream} the {@link OutputStream} used to write the document
     */
    public StreamDocRootWriter(final OutputStream stream,
                               final int maxColumns,
                               final String indentation,
                               final String newline,
                               final String copyrightNotice) {
        this(new PrintStream(stream), maxColumns, indentation, newline, copyrightNotice);
    }

    /**
     * Creates a {@link DocRootWriter} from the instance of {@link PrintStream}.
     *
     * @param stream the {@link PrintStream} to use when writing the documentation
     */
    public StreamDocRootWriter(final PrintStream stream,
                               final int maxColumns,
                               final String indentation,
                               final String newline,
                               final String copyrightNotice) {
        this.stream = stream;
        this.indentation = indentation;
        this.newline = newline;
        this.maxColumns = maxColumns;
        this.copyrightNotice = copyrightNotice;
    }

    @Override
    public PrintStream ps() {
        return stream;
    }

    @Override
    public Indentation indent() {

        final var indentation = new Indentation() {

            boolean open = true;

            @Override
            public void close() {
                if (open) {

                    open = false;

                    if (indentations.getLast() != this) {
                        throw new IllegalStateException("");
                    }

                    indentations.removeLast();

                }
            }

            @Override
            public String getPrefix() {
                return range(0, indentations.size())
                    .mapToObj(i -> StreamDocRootWriter.this.indentation)
                    .collect(joining());
            }

            @Override
            public String toString() {
                return getPrefix();
            }

        };

        indentations.addLast(indentation);
        return indentation;

    }

    @Override
    public Indentation getIndent() {
        return indentations.isEmpty() ? Indentation.DEFAULT : indentations.getLast();
    }

    @Override
    public int getMaxColumns() {
        return maxColumns;
    }

    @Override
    public String getNewline() {
        return newline;
    }

    @Override
    public String getCopyrightNotice() {
        return copyrightNotice;
    }

    @Override
    public void close() {
        ps().close();
    }

}
