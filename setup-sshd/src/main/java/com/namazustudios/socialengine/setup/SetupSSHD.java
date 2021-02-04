package com.namazustudios.socialengine.setup;

import org.apache.sshd.server.SshServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

import static java.lang.Thread.interrupted;

public class SetupSSHD {

    public static final String SSH_PORT = "com.namazustudios.socialengine.setup.sshd.port";

    public static final String SSH_HOST = "com.namazustudios.socialengine.setup.sshd.host";

    private static final Logger logger = LoggerFactory.getLogger(SetupSSHD.class);

    private final SshServer sshServer;

    private final Object lock = new Object();

    @Inject
    public SetupSSHD(final SshServer sshServer) {
        this.sshServer = sshServer;
    }

    public void start() throws IOException {
        sshServer.start();
    }

    public void stop() throws IOException {
        sshServer.stop();
    }

    public void run() throws IOException {

        start();

        try {
            while (!interrupted() && sshServer.isStarted()) {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        logger.info("Interrupted. Shutting down.");
                    }
                }
            }
        } finally {
            stop();
        }

    }

}
