package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterNodeMain {

    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeMain.class);

    public static void main(String[] args) throws InterruptedException {

        logger.info("Started Cluster Node Worker.");
        // TODO This is placeholder to facilitate the creation of a Docker container for the server

        final Object lock = new Object();

        synchronized (lock) {

            // Waits for process to be interrupted

            while (true) {
                lock.wait();
            }

        }

    }

}
