package dev.getelements.elements.sdk.mongo.test;

import java.io.IOException;
import java.nio.file.attribute.PosixFilePermissions;

import static java.lang.String.format;
import static java.nio.file.Files.setPosixFilePermissions;

public class SslCliMongoTestInstance extends CliMongoTestInstance {

    public SslCliMongoTestInstance(final int port) {
        this(port, ELEMENTS_TESTED_VERSION);
    }

    public SslCliMongoTestInstance(final int port, final String version) {
        super(port, version);
    }

    @Override
    protected Process newProcess(final String uuid) throws IOException {

        final var certificates = new MongoTestSslCertificates();

        final var directory = certificates.getDirectory();
        final var permissions = PosixFilePermissions.fromString("rwxr-xr-x");
        setPosixFilePermissions(directory, permissions);

        return new ProcessBuilder()
                .command(
                        "docker",
                        "run",
                        "--name",
                        uuid,
                        "--rm",
                        "--volume",
                        format("%s:%s", certificates.getDirectory(), "/etc/mongod.conf.d"),
                        format("-p%d:27017", port),
                        format("mongo:%s", version)
                        ,
                        "--tlsMode",
                        "requireTLS",
                        "--tlsCertificateKeyFile",
                        "/etc/mongod.conf.d/server.pem"
                )
                .redirectErrorStream(true)
                .start();
    }

    @Override
    protected void kill(final Process process, final String uuid) {
        DockerMongoTestInstance.doKill(process, uuid);
    }

}
