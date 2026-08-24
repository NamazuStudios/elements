package dev.getelements.elements.sdk.mongo.test;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.sdk.util.ShutdownHooks;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
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

public abstract class CliMongoTestInstance implements MongoTestInstance {

    private static final Logger logger = LoggerFactory.getLogger(CliMongoTestInstance.class);

    private static final ShutdownHooks hooks = new ShutdownHooks(CliMongoTestInstance.class);

    private static final int CONNECT_POLLING_RATE = 1000;

    private static final int CONNECT_POLLING_CYCLES = 300;

    protected final int port;

    protected final String version;

    private final Lock lock = new ReentrantLock();

    private String uuid;

    private Process process;

    private boolean running;

    public CliMongoTestInstance(final int port, final String version) {
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
            final var process = newProcess(uuid);
            runProcess(process);

            hooks.add(this::close);
            waitForConnect(port);

            final var initializeProcess = newInitializeProcess(uuid);

            if (initializeProcess != null) {

                runProcess(initializeProcess);
                initializeProcess.waitFor();

                final int initializeProcessExit = initializeProcess.exitValue();

                if (initializeProcessExit == 0) {
                    logger.info("Mongo Initializer process exited with code: 0");
                } else {
                    logger.error("Mongo Initializer process exited with code: {}", initializeProcessExit);
                }

            }

            waitForPrimary(port);

            this.uuid = uuid;
            this.process = process;
            this.running = true;

        } catch (IOException | InterruptedException ex) {
            throw new InternalException("Caught exception running Mongo Test Instance.", ex);
        }

    }

    private void runProcess(final Process process) {

        final var stdout = new Thread(log(process::getInputStream, m -> logger.info("mongod {}", m)));
        stdout.setDaemon(true);
        stdout.start();

        final var stderr = new Thread(log(process::getErrorStream, m -> logger.error("mongod {}", m)));
        stderr.setDaemon(true);
        stderr.start();

    }

    protected abstract Process newProcess(final String uuid) throws IOException;

    protected Process newInitializeProcess(final String uuid) throws IOException {
        return null;
    }

    @Override
    public void close() {
        kill(process, uuid);
    }

    protected abstract void kill(Process process, String uuid);

    private void waitForConnect(final int port) throws InterruptedException, UnknownHostException {

        final var addr = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});

        for (int i = 0; i < CONNECT_POLLING_CYCLES; ++i) {
            try (final var socket = new Socket(addr, port)) {
                break;
            } catch (IOException e) {
                sleep(CONNECT_POLLING_RATE);
            }
        }

    }

    /**
     * A single-node replica set accepts the connection and returns from {@code rs.initiate()} well before it
     * finishes electing itself PRIMARY. Writing (e.g. Guice eager singletons in test bootstrap) before that
     * election completes fails with {@code MongoNotPrimaryException}, so poll {@code hello} until the server
     * reports itself writable.
     */
    private void waitForPrimary(final int port) throws InterruptedException {

        final var connectionString = format("mongodb://127.0.0.1:%d/?connectTimeoutMS=1000&serverSelectionTimeoutMS=1000", port);

        for (int i = 0; i < CONNECT_POLLING_CYCLES; ++i) {
            try (final var client = MongoClients.create(connectionString)) {

                final var hello = client.getDatabase("admin")
                        .runCommand(new BsonDocument("hello", new BsonInt32(1)));

                final var writablePrimary = hello.containsKey("isWritablePrimary")
                        ? hello.get("isWritablePrimary")
                        : hello.get("ismaster");

                if (Boolean.TRUE.equals(writablePrimary)) {
                    return;
                }

            } catch (MongoException ex) {
                logger.debug("Mongo not yet ready for writes.", ex);
            }

            sleep(CONNECT_POLLING_RATE);
        }

        logger.warn("Timed out waiting for Mongo instance on port {} to become writable primary.", port);

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
