package dev.getelements.elements.setup.provider;

import dev.getelements.elements.setup.jline.ShellCompleter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static org.jline.reader.LineReader.Option.*;

public class LineReaderProvider implements Provider <LineReader> {

    private Provider<Terminal> terminalProvider;

    @Override
    public LineReader get() {

        final var parser = new DefaultParser();
        final var completer = new ShellCompleter();
        final var highlighter = new DefaultHighlighter();

        return LineReaderBuilder.builder()
                .terminal(getTerminalProvider().get())
                .parser(parser)
                .completer(completer)
                .highlighter(highlighter)
                .option(EMPTY_WORD_OPTIONS, true)
                .option(INSERT_TAB, true)
                .option(DISABLE_EVENT_EXPANSION, true)
                .appName("Elements Setup Terminal")
            .build();

    }

    public Provider<Terminal> getTerminalProvider() {
        return terminalProvider;
    }

    @Inject
    public void setTerminalProvider(Provider<Terminal> terminalProvider) {
        this.terminalProvider = terminalProvider;
    }

}
