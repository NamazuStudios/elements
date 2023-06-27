package dev.getelements.elements.rt;

import dev.getelements.elements.rt.exception.AssetNotFoundException;
import dev.getelements.elements.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractAssetLoader implements AssetLoader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAssetLoader.class);

    private final AtomicBoolean open = new AtomicBoolean(true);

    private final AtomicInteger openStreams = new AtomicInteger();

    @Override
    public void close() {

        final int count = openStreams.get();

        if (!open.compareAndSet(true, false)) {
            logger.error("Already closed this instance {}.  Streams open: {}", this, count);
        } else if (count != 0) {
            logger.error("Cosing this instance {}.  Streams open: {}", this, count);
            doClose();
        } else {
            logger.info("Closing {}", this);
            doClose();
        }

    }

    /**
     * Override this to include more closing behavior.
     */
    protected void doClose() {}

    @Override
    public InputStream open(final Path path) {

        if (!open.get()) {
            throw new InternalException("asset loader not open.");
        }

        if (path.isWildcard()) {
            throw new IllegalArgumentException(path + " must not be a wildcard.");
        }

        final InputStream is = doOpen(path);
        openStreams.incrementAndGet();

        final BufferedInputStream bis = new BufferedInputStream(is) {

            final AtomicBoolean closed = new AtomicBoolean(false);

            @Override
            public void close() throws IOException {

                if (closed.compareAndSet(true, false)) {
                    openStreams.decrementAndGet();
                }

                super.close();

            }
        };

        return bis;

    }

    protected abstract InputStream doOpen(final Path path);

}
