package com.namazustudios.socialengine.rt.util;

import org.slf4j.Logger;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProcessLogger {

    private final String command;

    private final Process process;

    private final Logger logger;

    public ProcessLogger(final String command, final Process process, final Logger logger) {
        this.command = command;
        this.process = process;
        this.logger = logger;
    }

    public void start() {

        final var pid = process.pid();

        final var stdout = new Thread(log(process::getInputStream, m -> logger.info("{} {} {}", pid, command, m)));
        stdout.setDaemon(true);
        stdout.start();

        final var stderr = new Thread(log(process::getErrorStream, m -> logger.error("{} {} {}", pid, command, m)));
        stderr.setDaemon(true);
        stderr.start();

    }

    private Runnable log(final Supplier<InputStream> inputStreamSupplier,
                         final Consumer<String> messageConsumer) {
        return () -> {
            try (var r = new InputStreamReader(inputStreamSupplier.get());
                 var br = new BufferedReader(r)) {

                var line = br.readLine();

                while (line != null) {
                    messageConsumer.accept(line);
                    line = br.readLine();
                }

            } catch (EOFException ex) {
                logger.info("Hit end of stream.");
            } catch (IOException ex) {
                logger.info("Caught IO Exception reading subprocess.", ex);
            }
        };
    }

}
