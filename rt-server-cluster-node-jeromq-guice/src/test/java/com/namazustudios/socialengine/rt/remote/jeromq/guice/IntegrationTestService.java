package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.namazustudios.socialengine.rt.remote.TestServiceInterface;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class IntegrationTestService implements TestServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestService.class);

    private final ScheduledExecutorService scheduledExecutorService = newScheduledThreadPool(1);

    @Override
    public void testSyncVoid(final String msg) {

        final Random random = new Random();
        final int msec = random.nextInt(100);

        if (!"Hello".equals(msg)) {
            throw new IllegalArgumentException("Expected \"Hello\" but got: " + msg);
        }

        try {
            sleep(msec);
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

        final Random random = new Random();
        final int msec = random.nextInt(100);

        try {
            sleep(msec);
        } catch (InterruptedException e) {
            throw new InternalException(e);
        }

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

        final Random random = new Random();
        final int msec = random.nextInt(100);

        scheduledExecutorService.schedule(() -> {

            logger.info("{}: Got msg {}", new Exception().getStackTrace()[0], msg);

            if ("Hello".equals(msg)) {
                stringConsumer.accept("World!");
            } else {
                final IllegalArgumentException iae = new IllegalArgumentException("Expecting \"Hello\" but got " + msg);
                throwableConsumer.accept(iae);
            }

        }, msec, MILLISECONDS);
    }

    @Override
    public Future<Integer> testAsyncReturnFuture(final String msg) {

        final Random random = new Random();
        final int msec = random.nextInt(100);

        return scheduledExecutorService.schedule(() -> {

            logger.info("{}: Got msg {}", new Exception().getStackTrace()[0], msg);

            if ("Hello".equals(msg)) {
                return 42;
            } else {
                throw new IllegalArgumentException("Expecting \"Hello\" but got " + msg);
            }

        }, msec, MILLISECONDS);
    }

    @Override
    public Future<Integer> testAsyncReturnFuture(final String msg,
                                                 final Consumer<String> stringConsumer,
                                                 final Consumer<Throwable> throwableConsumer) {

        final Random random = new Random();
        final int msec = random.nextInt(100);

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

        }, msec, MILLISECONDS);
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

    @Override
    public String testEcho(final String msg, final double errorChance) {

        final Random random = new Random();
        final int msec = random.nextInt(100);
        final boolean errorneous = random.nextDouble() < errorChance;

        try {
            return scheduledExecutorService.schedule(() -> {

                if (errorneous) {
                    throw new IllegalArgumentException(msg);
                }

                return msg;

            }, msec, MILLISECONDS).get();
        } catch (InterruptedException e) {
            throw new InternalException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException)e.getCause();
            } else {
                throw new InternalException(e);
            }
        }

    }

}
