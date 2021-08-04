package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.annotation.Private;

import java.io.PrintStream;

import static com.namazustudios.socialengine.doclet.DocFormatting.*;
import static java.lang.String.format;

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
     * Prints a line of text.
     *
     * @param line the line to print
     * @return this instance
     */
    default DocRootWriter println(final Object line) {
        ps().print(line.toString());
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
        split(block, getMaxColumns(), prefix).forEach(this::println);
        return this;
    }

    /**
     * Prints a new line to the file.
     */
    default void println() {
        ps().print(getNewline());
    }

}
