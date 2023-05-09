package dev.getelements.elements.rt.remote.jeromq;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;

class JeroMQDebugCounter {

    private long value = 0;

    private LocalDateTime when = now();

    public void increment() {
        ++value;
        when = now();
    }

    public String toString() {
        return format("%s (last recorded@ %s)", Long.toUnsignedString(value), when);
    }

}
