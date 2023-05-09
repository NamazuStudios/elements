package dev.getelements.elements.setup;

import org.jline.reader.LineReader;

import javax.inject.Inject;

import static java.lang.String.format;

public class LineReaderSecureReader implements SecureReader {

    private final LineReader lineReader;

    @Inject
    public LineReaderSecureReader(final LineReader lineReader) {
        this.lineReader = lineReader;
    }

    @Override
    public String read(final String fmt, final Object... args) {
        final var prompt = format("%%{%s%%}", format(fmt, args));
        return lineReader.readLine(prompt);
    }

    @Override
    public String reads(final String fmt, final Object... args) {
        final var prompt = format("%%{%s%%}", format(fmt, args));
        return lineReader.readLine(prompt, '*');
    }

}
