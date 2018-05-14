package com.namazustudios.socialengine.rt.jeromq;

import org.slf4j.Logger;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMonitor;

import java.io.IOException;

public class MonitorThread extends Thread implements AutoCloseable {

    private final Logger logger;
    private final ZContext zContext;
    private final ZMQ.Socket monitored;

    public MonitorThread(final String name, final Logger logger, final ZContext zContext, final ZMQ.Socket monitored) {
        setName(name);
        setDaemon(true);
        this.logger = logger;
        this.zContext = ZContext.shadow(zContext);
        this.monitored = monitored;
    }

    @Override
    public void run() {
        try (final ZMonitor zMonitor = new ZMonitor(zContext, monitored).add(ZMonitor.Event.ALL).start()) {
            while (!interrupted()) {
                final ZMonitor.ZEvent zEvent = zMonitor.nextEvent();
                logger.info("Socket Event: {}", zEvent);
            }
        } catch (IOException e) {
            logger.error("Caught exception.", e);
        }
    }

    @Override
    public void close() {
        interrupt();
    }

}
