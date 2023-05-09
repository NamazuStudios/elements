package dev.getelements.elements.setup.commands;

import dev.getelements.elements.service.Unscoped;
import dev.getelements.elements.service.VersionService;
import dev.getelements.elements.setup.SetupCommand;
import dev.getelements.elements.setup.SetupCommands;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static java.lang.Thread.interrupted;

public class SetupShell implements SetupCommand {

    private static final Logger logger = LoggerFactory.getLogger(SetupShell.class);

    private Thread thread;

    private final Terminal terminal;

    private final LineReader lineReader;

    @Inject
    private Root root;

    @Inject
    @Unscoped
    private VersionService versionService;

    @Inject
    public SetupShell(final Terminal terminal, final LineReader lineReader) throws IOException {
        this.terminal = terminal;
        this.lineReader = lineReader;
    }

    public CompletionStage<Integer> start() {

        if (thread != null) throw new IllegalStateException();

        final var future = new CompletableFuture<Integer>();

        thread = new Thread(() -> {
            run(future);
            future.complete(0);
        });

        thread.start();

        return future.handleAsync((status, exception) -> {

            if (exception instanceof CancellationException) {
                thread.interrupt();
            }

            return status;

        });

    }

    @Override
    public void run(final String[] args) throws Exception {

        final var future = start().toCompletableFuture();

        try {
            future.get();
        } catch (ExecutionException ex) {
            throw (Exception) ex.getCause();
        }

    }

    public void close() throws Exception {

        if (thread != null) {
            thread.interrupt();
            thread.join();
        }

        thread = null;

    }

    private void run(final CompletableFuture<Integer> completableFuture) {

        final var version = versionService.getVersion();

        terminal.writer().println("Namazu Elements™ ©(2015 - 2021)");
        terminal.writer().println("Setup and Administration Terminal");
        terminal.writer().printf("Version: %s\n", version.getVersion());
        terminal.writer().printf("Revision: %s\n", version.getRevision());
        terminal.writer().printf("Timestamp: %s\n", version.getTimestamp());

        while(!interrupted()) {
            try {
                processCommand();
            } catch (EndOfFileException ex) {
                logger.debug("Got EOF", ex);
                completableFuture.complete(0);
                return;
            } catch (Exception ex) {
                completableFuture.complete(-1);
                return;
            }
        }

        completableFuture.complete(0);

    }

    private void processCommand() throws Exception {

        final var line = lineReader.readLine("%{Setup $ %}");
        final var args = Stream.of(line.split("\\s+")).map(String::trim).toArray(String[]::new);

        if (args.length > 0 && SetupCommands.SHELL.commandName.equals(args[0])) {
            logger.debug("Skipping recursive shell invocation.");
        } else {
            root.run(args);
        }

    }

}
