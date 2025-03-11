package dev.getelements.elements.rt.remote;

import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.util.ConcurrentLockedPublisher;
import dev.getelements.elements.sdk.util.Publisher;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

public class MockInstanceConnectionService implements InstanceConnectionService {

    private final Lock lock = spy(new ReentrantLock());

    private final Publisher<InstanceConnection> onConnectPublisher = spy(new ConcurrentLockedPublisher<>(lock));

    private final Publisher<InstanceConnection> onDisconnectPublisher = spy(new ConcurrentLockedPublisher<>(lock));

    @Override
    public InstanceId getInstanceId() {
        return null;
    }

    @Override
    public String getLocalControlAddress() {
        return null;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void refresh() {}

    @Override
    public InstanceBinding openBinding(NodeId nodeId) {
        return null;
    }

    @Override
    public List<InstanceConnection> getActiveConnections() {
        return emptyList();
    }

    @Override
    public Subscription subscribeToConnect(Consumer<InstanceConnection> onConnect) {
        return getOnConnectPublisher().subscribe(onConnect);
    }

    @Override
    public Subscription subscribeToDisconnect(Consumer<InstanceConnection> onDisconnect) {
        return getOnDisconnectPublisher().subscribe(onDisconnect);
    }

    public Lock getLock() {
        return lock;
    }

    public Publisher<InstanceConnection> getOnConnectPublisher() {
        return onConnectPublisher;
    }

    public Publisher<InstanceConnection> getOnDisconnectPublisher() {
        return onDisconnectPublisher;
    }

    public void resetInternal() {
        reset(getLock(), getOnConnectPublisher(), getOnDisconnectPublisher());
    }

}
