package com.namazustudios.socialengine.rt.jeromq;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMonitor;
import zmq.Command;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.zeromq.ZContext.shadow;

public class JeroMQMonitorThread extends Thread implements AutoCloseable {

    private static final long EVENT_LOG_TIME_MSEC = MILLISECONDS.convert(10, SECONDS);

    private final Logger logger;

    private final ZContext zContext;

    private final ZMQ.Socket monitored;

    public JeroMQMonitorThread(final String name,
                               final Logger logger,
                               final ZContext zContext,
                               final ZMQ.Socket monitored) {
        setName(name);
        setDaemon(true);
        this.logger = logger;
        this.zContext = zContext;
        this.monitored = monitored;
    }

    @Override
    public void run() {
        try (final ZContext shadow = shadow(zContext);
             final ZMonitor zMonitor = new ZMonitor(shadow, monitored).add(ZMonitor.Event.ALL).start()) {

            final Stopwatch stopwatch = Stopwatch.createStarted();
            final Map<String, Integer> zEventCountMap = new LinkedHashMap<>();

            while (!interrupted()) {

                final int timeout = max(0, (int)(EVENT_LOG_TIME_MSEC - stopwatch.elapsed(MILLISECONDS)));
                final ZMonitor.ZEvent zEvent = zMonitor.nextEvent(timeout);

                if (zEvent != null) switch (zEvent.type) {
                    case CONNECTED:
                    case BIND_FAILED:
                    case DISCONNECTED:
                    case HANDSHAKE_PROTOCOL:
                        logger.info("Socket Event: {}", zEvent);
                        break;
                    default:
                        final String zEventString = format("Event %s from Address %s", zEvent.type, zEvent.address);
                        zEventCountMap.compute(zEventString, (e, i) -> i == null ? 1 : i + 1);
                }

                if (stopwatch.elapsed(MILLISECONDS) >= EVENT_LOG_TIME_MSEC) {
                    zEventCountMap.forEach((ev, c) -> logger.info("Socket Event: {} ({} times)", ev, c));
                    zEventCountMap.clear();
                    stopwatch.reset().start();
                }

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
