package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Thread.interrupted;
import static java.util.UUID.randomUUID;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class TestJMQ {

    private static final int NTESTS = 2;

    public static void main(final String[] args) throws Exception {

        final String addr = "inproc://helloworld-" + randomUUID();

        final ZContext c = new ZContext();

        final Thread echoer = new Thread(() -> {
            try (final ZMQ.Poller poller = c.createPoller(1);
                 final ZMQ.Socket socket = c.createSocket(ZMQ.ROUTER)) {

                socket.bind(addr);

                final int i = poller.register(socket);

                while (!interrupted()) {

                    final int nevents = poller.poll(1000);

                    if (nevents <= 0) {
                        continue;
                    } else if (poller.pollin(i)) {
                        final ZMsg msg = ZMsg.recvMsg(socket);
                        msg.send(socket);
                        System.out.println("Echoing request.");
                    } else if (poller.pollerr(i)) {
                        throw new RuntimeException("Got socket error.");
                    }

                }

            }
        });

        echoer.setDaemon(true);
        echoer.start();

        final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
            final Thread th = new Thread(r);
            th.setDaemon(true);
            return th;
        });

        final ExecutorCompletionService<Integer> executorCompletionService;
        executorCompletionService = new ExecutorCompletionService<>(executorService);

        final Set<Future<Integer>> futureSet = new HashSet<>();

        for (int i = 0; i < NTESTS; ++i) {
            final int iteration = i;

            final Future<Integer> booleanFuture = executorCompletionService.submit(() -> {

                Runnable destroy = () -> {};

                try (final ZMQ.Socket socket = c.createSocket(ZMQ.DEALER);
                     final ZMQ.Poller poller = c.createPoller(1)) {

                    final int index = poller.register(socket, POLLIN | POLLERR);

                    socket.connect(addr);
                    destroy = () -> c.destroySocket(socket);

                    final String uuid = randomUUID().toString();

                    final ZMsg request = new ZMsg();
                    request.addLast(uuid);
                    request.send(socket);

                    while (!interrupted()) {
                        if (poller.poll(1) < 0) {
                            break;
                        } else if (poller.pollin(index)) {
                            break;
                        } else {
                            throw new RuntimeException();
                        }
                    }

                    final ZMsg response = ZMsg.recvMsg(socket);

                    if (Objects.equals(uuid, response.getFirst().getString(ZMQ.CHARSET))) {
                        return iteration;
                    } else {
                        throw new RuntimeException();
                    }

                } finally {
                    destroy.run();
                }
            });

            futureSet.add(booleanFuture);

        }

        int count = 0;
        Future<Integer> f;

        do {
            f = executorCompletionService.take();
            System.out.printf("Test Succeeded: %d\n", f.get());
            futureSet.remove(f);
        } while (!futureSet.isEmpty());

        System.out.printf("Completed all tests.");

        echoer.interrupt();
        echoer.join();

    }

}
