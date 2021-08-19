package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.annotation.Private;

import java.io.IOException;
import java.io.PrintStream;

import static com.namazustudios.socialengine.doclet.DocFormatting.*;

/**
 * Writes a {@link DocRoot} to persistent storage.
 */
@Private
public interface DocRootWriter extends AutoCloseable {

    /**
     * Gets the PrintStream used to write out the document. The returned {@link PrintStream} will always be the same
     * and closing this {@link DocWriter} will also close the stream.
     *
     * @return the {@link PrintStream} written to write the
     */
    PrintStream ps();

    /**
     * Opens a new level of indentation, returning the instance of {@link Indentation}.
     *
     * @return the {@link Indentation}
     */
    Indentation indent();

    /**
     * Gets the current {@link Indentation}.
     *
     * @return the indentation prefix
     */
    Indentation getIndent();

    /**
     * Gets the maximum number of columns to print per line, where possible.
     *
     * @return the max columns to print per line.
     */
    int getMaxColumns();

    /**
     * Gets the preferred newline character or set of characters.
     *
     * @return the newline characters or set of characters
     */
    String getNewline();

    /**
     * Gets the copyright notice which is configured for this {@link DocRootWriter}.
     *
     * @return the copyright notice
     */
    String getCopyrightNotice();

    /**
     * Prints the copyright notice and other standard metadata.
     *
     * @param prefix the prefix for the copyright notice.
     */
    default void printCopyrightNotice(String prefix) {
        printBlock(prefix, getCopyrightNotice());
    }


    /**
     * Prints a line of text.
     *
     * @param line the line to print
     * @return this instance
     */
    default DocRootWriter println(final Object line) {

        final var str = line.toString().trim();

        if (!str.isEmpty()) {
            ps().print(getIndent().getPrefix());
            ps().print(line.toString());
        }

        ps().print(getNewline());
        return this;

    }

    /**
     * Format-prints a line, automatically appending this {@link DocRootWriter}'s preferred end of line character.
     *
     * @param fmt the format string
     * @param args the args to append
     *
     * @return this instance
     */
    default DocRootWriter printlnf(final String fmt, final Object ... args) {
        ps().print(getIndent().getPrefix());
        ps().printf(fmt, args).print(getNewline());
        return this;
    }

    /**
     * Splits the supplied block into multiple lines, if necessary, and the prints the block line by line.
     *
     * @param prefix the prefix to print
     * @param block the block of text to print
     *
     * @return this instance
     */
    default DocRootWriter printBlock(final String prefix, final String block) {

        split(block, getMaxColumns(), getIndent().getPrefix() + prefix).forEach(line -> {
            ps().print(line);
            ps().print(getNewline());
        });

        return this;

    }

    /**
     * Prints a new line to the file.
     */
    default void println() {
        ps().print(getNewline());
    }

    @Override
    default void close() throws IOException {}

    /**
     * Represents the current indentation of the associated {@link DocRootWriter}
     */
    interface Indentation extends AutoCloseable {

        void close();

        String getPrefix();

        Indentation DEFAULT = new Indentation() {

            @Override
            public void close() {}

            @Override
            public String getPrefix() {
                return "";
            }

        };
    }

}
