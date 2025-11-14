package dev.getelements.elements.sdk.mongo.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static java.lang.String.format;

public class DockerMongoTestInstance extends CliMongoTestInstance {

    private static final Logger logger = LoggerFactory.getLogger(DockerMongoTestInstance.class);

    public DockerMongoTestInstance(final int port) {
        super(port, ELEMENTS_TESTED_VERSION);
    }

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
                        format("integration-test-%s", uuid)
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
