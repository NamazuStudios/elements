package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.namazustudios.socialengine.remote.TestServiceInterface;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

public class IntegrationTestService implements TestServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestService.class);

    private final ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();

    @Override
    public void testSyncVoid(final String msg) {
        try {
            logger.info("{}: Got msg {}", new Exception().getStackTrace()[0], msg);
            scheduledExecutorService.schedule(() -> logger.info("Got msg {}", msg), 1, SECONDS).get();
        } catch (InterruptedException e) {
            throw new InternalException(e);
        } catch (ExecutionException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public double testSyncReturn(String msg) {

        logger.info("{}: Got msg {}", new Exception().getStackTrace()[0], msg);

        if ("Hello".equals(msg)) {
            return 40.42;
        } else {
            throw new IllegalArgumentException("Expecting \"Hello\" but got " + msg);
        }

    }

    @Override
    public void testAsyncReturnVoid(final String msg,
                                    final Consumer<String> stringConsumer,
                                    final Consumer<Throwable> throwableConsumer) {
        scheduledExecutorService.schedule(() -> {

            logger.info("{}: Got msg {}", new Exception().getStackTrace()[0], msg);

            if ("Hello".equals(msg)) {
                stringConsumer.accept("World!");
            } else {
                final IllegalArgumentException iae = new IllegalArgumentException("Expecting \"Hello\" but got " + msg);
                throwableConsumer.accept(iae);
            }

        }, 1, SECONDS);
    }

    @Override
    public Future<Integer> testAsyncReturnFuture(final String msg) {
        return scheduledExecutorService.schedule(() -> {

            logger.info("{}: Got msg {}", new Exception().getStackTrace()[0], msg);

            if ("Hello".equals(msg)) {
                return 42;
            } else {
                throw new IllegalArgumentException("Expecting \"Hello\" but got " + msg);
            }

        }, 1, SECONDS);
    }

    @Override
    public Future<Integer> testAsyncReturnFuture(final String msg,
                                                 final Consumer<String> stringConsumer,
                                                 final Consumer<Throwable> throwableConsumer) {
        return scheduledExecutorService.schedule(() -> {

            logger.info("{}: Got msg {}", new Exception().getStackTrace()[0], msg);

            try {
                if ("Hello".equals(msg)) {
                    stringConsumer.accept("World!");
                    return 42;
                } else {
                    throw new IllegalArgumentException("Expecting \"Hello\" but got " + msg);
                }
            } catch (IllegalArgumentException iae) {
                throwableConsumer.accept(iae);
                throw iae;
            }

        }, 1, SECONDS);
    }

    @Override
    public Future<Integer> testAsyncReturnFuture(final String msg,
                                                 final MyStringHandler stringConsumer,
                                                 final MyErrorHandler errorHandler) {
        return testAsyncReturnFuture(
            msg,
            (Consumer<String>) stringConsumer::handle,
            (Consumer<Throwable>) errorHandler::handle);
    }

}
