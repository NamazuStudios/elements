package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.rt.util.Monitor;
import dev.getelements.elements.rt.util.ShutdownHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;

public class DockerMongoTestInstance implements MongoTestInstance {

    private static final Logger logger = LoggerFactory.getLogger(DockerMongoTestInstance.class);

    private static final ShutdownHooks hooks = new ShutdownHooks(DockerMongoTestInstance.class);

    private static final int CONNECT_POLLING_RATE = 1000;

    private static final int CONNECT_POLLING_CYCLES = 300;

    private final Lock lock = new ReentrantLock();

    private String uuid;

    private Process process;

    private boolean running;

    private final int port;

    private final String version;

    public DockerMongoTestInstance(final int port, final String version) {
        this.port = port;
        this.version = version;
    }

    @Override
    public void start() {

        uuid = format("%s_%s", getClass().getSimpleName(), randomUUID());

        try (var _m = Monitor.enter(lock)) {

            if (this.running) {
                logger.error("Already running.");
                throw new IllegalStateException("Already running.");
            }

            logger.info("Starting test mongo process via Docker.");

            final var uuid = format("%s_%s", getClass().getSimpleName(), randomUUID());

            final var process = new ProcessBuilder()
                    .command(
                            "docker",
                            "run",
                            "--name",
                            uuid,
                            "--rm",
                            format("-p%d:27017", port),
                            format("mongo:%s", version)
                    )
                    .redirectErrorStream(true)
                    .start();

            final var stdout = new Thread(log(process::getInputStream, m -> logger.info("mongod {}", m)));
            stdout.setDaemon(true);
            stdout.start();

            final var stderr = new Thread(log(process::getErrorStream, m -> logger.error("mongod {}", m)));
            stderr.setDaemon(true);
            stderr.start();

            hooks.add(this::close);
            waitForConnect(port);

            this.uuid = uuid;
            this.process = process;
            this.running = true;

        } catch (IOException | InterruptedException ex) {
            throw new InternalException("Caught exception running Mongo Test Instance.", ex);
        }

    }

    @Override
    public void close() {

        try {

            logger.info("Destroying mongo process.");
            process.destroy();

            final var kill = new ProcessBuilder()
                    .command("docker", "kill", uuid)
                    .start();

            kill.waitFor();

        } catch (IOException | InterruptedException ex) {
            logger.error("Caught exception shutting down.", ex);
        }

    }

    private void waitForConnect(final int port) throws InterruptedException, UnknownHostException {

        final var addr = InetAddress.getByAddress(new byte[]{127,0,0,1});

        for (int i = 0; i < CONNECT_POLLING_CYCLES; ++i) {
            try (final var socket = new Socket(addr, port)) {
                break;
            } catch (IOException e) {
                sleep(CONNECT_POLLING_RATE);
            }
        }

    }

    public Runnable log(final Supplier<InputStream> inputStreamSupplier,
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
