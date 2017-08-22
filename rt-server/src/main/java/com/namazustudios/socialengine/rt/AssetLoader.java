package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

/**
 * Used to load the various assets for the underlying scripting engine.  This is responsible
 * for providing a raw access to the raw server-side assets.  This allows for the opening of
 * files relative to the root path of the application.
 *
 * This may or may not actually live on a real filesystem.  This can provide, for example,
 * access to an archive/zip file or a virtual file system.
 *
 * Unless otherwise specified, implementations of this type should be considered thread safe insofar as
 * that they must expect multiple threads my access this instance at any given time.  However, the
 * returned {@link InputStream} need not be anymore thread safe than the specification of {@link InputStream}
 * itself.
 *
 * Created by patricktwohig on 8/14/17.
 */
public interface AssetLoader extends AutoCloseable {

    /**
     * Closes the {@link AssetLoader} and cleaning up any resources.  Any open {@link InputStream}
     * instances may be closed, but this is not a guarantee.  All resources open <b>should</b> be closed
     * before closing this {@link AssetLoader}.  Using resources after closing this instance, or
     * closing this instance while resources are open should be considered undefined behavior.
     *
     * Invoking this method twice on the same object should also be considered undefined behavior.
     */
    @Override
    void close();

    /**
     * Reads an asset as a String.  {@link #open(Path)}
     *
     * @param pathString the path string
     * @return an {@link InputStream} used to read the underlying asset
     */
    default InputStream open(final String pathString) {
        final Path path = new Path(pathString);
        return open(path);
    }

    /**
     * Opens a {@link InputStream} to an asset on the application's path.
     *
     * @param path the {@link Path} to the file.
     * @return an {@link InputStream}
     */
    InputStream open(Path path);

    /**
     * Returns a {@link AssetLoader} that is reference counted with a no-op close {@link Consumer}.
     *
     * {@see {@link #getReferenceCountedView(Consumer)}} for more details.
     *
     * @return a view of this {@link AssetLoader} which reference counts the underlying instance
     */
    default AssetLoader getReferenceCountedView() {
        return getReferenceCountedView(l -> {});
    }

    /**
     * Returns a view of this {@link AssetLoader} which will reference count the original underlying
     * {@link AssetLoader}.  This will ensure that multiple dependent types can each manage their own
     * reference and the last-used type will fully invoke the underlying {@link #close()} method.
     *
     * Invoking this method on the returned instance will increment the unerlying reference count,
     * and invoking {@link #close()} will decrement the reference count.  Invoking {@link #close()}
     * on the last instance will finally invoke {@link #close()} on the underlying instance and
     * permanently close the loader.
     *
     * The reference counted view of the underlying {@link AssetLoader} shoudl be considered a lightweight
     * object.
     *
     * @param onFinalClose called when the final underlying {@link AssetLoader} is closed
     * @return a view of this {@link AssetLoader} which reference counts the underlying instance
     */

    default AssetLoader getReferenceCountedView(final Consumer<AssetLoader> onFinalClose) {

        final AssetLoader instance = this;
        final AtomicInteger refCount = new AtomicInteger(0);
        final Logger logger = LoggerFactory.getLogger(instance.getClass());

        return new AssetLoader() {

            private Consumer<AssetLoader> onFinalCloseChain = onFinalClose;

            @Override
            public void close() {

                final int count = updateRefCount(c -> c - 1);

                if (count == 0) {
                    logger.info("{} closed ref-counted asset loader normally", instance);
                    instance.close();
                    onFinalClose.accept(instance);
                } else if (count < 0) {
                    // This should never happen, but in case it does we should log an error
                    // indicating so.  The wrapped instance will possibly throw an exception
                    // so we want to defer to its logic, but ensure that an error is logged.
                    logger.error("{} got less than zero reference count for {}", instance, count);
                    instance.close();
                } else {
                    logger.info("{} decremented reference count {}", instance, count);
                }

            }

            @Override
            public InputStream open(Path path) {
                return instance.open(path);
            }

            public AssetLoader getReferenceCountedView(final Consumer<AssetLoader> onFinalClose) {
                onFinalCloseChain = onFinalCloseChain.andThen(onFinalClose);
                updateRefCount(count -> count + 1);
                return this;
            }

            private int updateRefCount(final IntUnaryOperator intUnaryOperator) {
                return refCount.getAndUpdate(intUnaryOperator.andThen(count -> {

                    if (count <= 0) {
                        throw new IllegalStateException("Already closed " + instance);
                    }

                    return count;

                }));
            }

        };

    }

}
