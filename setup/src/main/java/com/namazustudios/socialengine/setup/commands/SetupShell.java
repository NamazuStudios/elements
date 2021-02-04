package com.namazustudios.socialengine.setup.commands;

import com.namazustudios.socialengine.service.Unscoped;
import com.namazustudios.socialengine.service.VersionService;
import com.namazustudios.socialengine.setup.SecureReader;
import com.namazustudios.socialengine.setup.SetupCommand;
import com.namazustudios.socialengine.setup.SetupCommands;
import com.namazustudios.socialengine.setup.jline.ShellCompleter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Exchanger;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.jline.reader.LineReader.Option.*;

public class SetupShell implements SetupCommand, SecureReader {

    private static final Logger logger = LoggerFactory.getLogger(SetupShell.class);

    private final Terminal terminal;

    private final LineReader reader;

    private final Thread thread = new Thread(this::run);

    private final Exchanger<Exception> exceptionExchanger = new Exchanger<>();

    @Inject
    private Root root;

    @Inject
    @Unscoped
    private VersionService versionService;

    @Inject
    public SetupShell() throws IOException {
        this(System.in, System.out);
    }

    public SetupShell(final InputStream stdin, final OutputStream stdout) throws IOException {

        final var attributes = new Attributes();

        terminal = TerminalBuilder.builder()
                .attributes(attributes)
                .name("Elements Setup Terminal")
                .system(false)
                .streams(stdin, stdout)
            .build();

        final var parser = new DefaultParser();
        final var completer = new ShellCompleter();
        final var highlighter = new DefaultHighlighter();

        reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(parser)
                .completer(completer)
                .highlighter(highlighter)
                .option(EMPTY_WORD_OPTIONS, true)
                .option(INSERT_TAB, true)
                .option(DISABLE_EVENT_EXPANSION, true)
                .appName("Elements Setup Terminal")
            .build();

    }

    @Override
    public void run(final String[] args) throws Exception {

        thread.start();

        final var result = exceptionExchanger.exchange(null);
        if (result != null) throw result;

    }

    @Override
    public String reads(final String fmt, final Object... args) {
        final var prompt = format("%%{%s%%}", format(fmt, args));
        return reader.readLine(prompt, '*');
    }

    public void close() throws Exception {
        thread.interrupt();
        thread.join();
    }

    private void run() {

        try {

            final var version = versionService.getVersion();

            terminal.writer().println("Namazu Elements™ ©(2015 - 2021)");
            terminal.writer().printf("Version: %s\n", version.getVersion());
            terminal.writer().printf("Revision: %s\n", version.getRevision());
            terminal.writer().printf("Timestamp: %s\n", version.getTimestamp());

            while(!interrupted()) {
                try {
                    processCommand();
                } catch (EndOfFileException ex) {
                    logger.debug("Got EOF", ex);
                    exceptionExchanger.exchange(null);
                    return;
                } catch (Exception ex) {
                    exceptionExchanger.exchange(ex);
                    return;
                }
            }

            exceptionExchanger.exchange(null);

        } catch (InterruptedException ex) {
            logger.error("Interrupted exchanging exception", ex);
        }

    }

    private void processCommand() throws Exception {

        final var line = reader.readLine("%{Namazu Elements™ $ %}");
        final var args = Stream.of(line.split("\\s+")).map(String::trim).toArray(String[]::new);

        if (args.length > 0 && SetupCommands.SHELL.commandName.equals(args[0])) {
            logger.debug("Skipping recursive shell invocation.");
        } else {
            root.run(args);
        }

    }

}
