package com.namazustudios.socialengine;

import com.google.common.base.Stopwatch;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Constants.HANDLER_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static java.lang.String.format;
import static java.lang.Thread.sleep;

public class SerialStressTest {

    private static final Logger logger = LoggerFactory.getLogger(SerialStressTest.class);

    public static void main(String[] args) throws InterruptedException {
        final Injector injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                install(new LuaModule() {
                    @Override
                    protected void configureFeatures() {
                        enableAllFeatures();
                    }
                });

                install(new SimpleContextModule());

                bind(IocResolver.class).to(GuiceIoCResolver.class).asEagerSingleton();
                bind(AssetLoader.class).to(ClasspathAssetLoader.class).asEagerSingleton();
                bind(Integer.class).annotatedWith(named(SCHEDULER_THREADS)).toInstance(1);
                bind(Long.class).annotatedWith(named(HANDLER_TIMEOUT_MSEC)).toInstance(90l);

            }
        });

        final AtomicInteger tc = new AtomicInteger();
        final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(format("TestThread #%d", tc.incrementAndGet()));
            thread.setUncaughtExceptionHandler((t, e) -> logger.error("Caught error", e));
            return thread;
        });

        int count = 0;
        int recurse = 0;
        boolean dbg = false;
        boolean ready = false;

        do {
            try {
                final Scanner scanner = new Scanner(System.in);

                System.out.println("How many threads?");
                count = scanner.nextInt();

                System.out.println("How many levels of recursion?");
                recurse = scanner.nextInt();

                System.out.println("Debug?");
                dbg = scanner.nextBoolean();

                ready = true;
            } catch (NumberFormatException ex) {
                System.out.print("Invalid number.");
            }
        } while (!ready);

        for (int i = 0; i < count; ++i) {
            final TestRunnable runnable = injector.getInstance(TestRunnable.class);
            runnable.setDebug(dbg);
            runnable.setRecurse(recurse);
            executorService.submit(runnable);
        }

        final Object lock = new Object();

        synchronized (lock) {
            while (true) lock.wait();
        }

    }

    private static class TestRunnable implements Runnable {

        private int recurse;

        private boolean debug;

        private ResourceLoader resourceLoader;

        @Override
        public void run() {
            try {
                int count = 0;
                final Stopwatch stopwatch = Stopwatch.createStarted();

                while (true) try {

                    recurseAndTest(0);
                    count++;

                    if (stopwatch.elapsed(TimeUnit.SECONDS) >= 10) {
                        logger.info("Processed {} executions.", count);
                        stopwatch.reset().start();
                    }

                } catch (Exception ex) {
                    logger.error("Caught exception.", ex);
                } finally {
                    sleep(30);
                }
            } catch (InterruptedException ex) {
                logger.info("Interrupted.  Exiting.");
            }
        }

        private void recurseAndTest(int recursion) throws IOException {
            if (recursion < getRecurse()) recurseAndTest(recursion + 1);
            else testIteration();
        }

        private void testIteration() throws IOException {

            final byte[] bytes;

            try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 final Resource resource = getResourceLoader().load("stresstest")) {
                resource.setVerbose(true);
                resource.serialize(bos);
                bytes = bos.toByteArray();
                logger.trace("Successfully saved {} ", resource);
            }

            try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 final Resource resource = getResourceLoader().load(bis, isDebug())) {
                logger.trace("Successfully loaded {} ", resource);
            }

        }

        public int getRecurse() {
            return recurse;
        }

        public void setRecurse(int recurse) {
            this.recurse = recurse;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public ResourceLoader getResourceLoader() {
            return resourceLoader;
        }

        @Inject
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

    }

}
