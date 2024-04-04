package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.profile.Profile;
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

public class DockerMongoTestInstance extends CliMongoTestInstance {

    private static final Logger logger = LoggerFactory.getLogger(DockerMongoTestInstance.class);

    public DockerMongoTestInstance(final int port, final String version) {
        super(port, version);
    }

    @Override
    protected Process newProcess(final String uuid) throws IOException {
        return new ProcessBuilder()
                .command(
                        "docker",
                        "run",
                        "--name",
                        uuid,
                        "--rm",
                        format("-p%d:27017", port),
                        format("mongo:%s", version),
                        "--replSet",
                        "integration-test"
                )
                .redirectErrorStream(true)
                .start();
    }

    @Override
    protected Process newInitializeProcess(final String uuid) throws IOException {
        return new ProcessBuilder()
                .command(
                        "docker",
                        "exec",
                        uuid,
                        "mongosh",
                        "--eval",
                        "rs.initiate()"
                )
                .redirectErrorStream(true)
                .start();
//        mongosh --eval "rs.initiate()"
    }

    @Override
    protected void kill(final Process process, final String uuid) {
        DockerMongoTestInstance.doKill(process, uuid);
    }

    public static void doKill(final Process process, final String uuid) {
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
}
