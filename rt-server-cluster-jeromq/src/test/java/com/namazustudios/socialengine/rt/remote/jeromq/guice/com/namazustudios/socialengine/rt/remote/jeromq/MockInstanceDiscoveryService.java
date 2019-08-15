package com.namazustudios.socialengine.rt.remote.jeromq.guice.com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.remote.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.remote.InstanceHostInfo;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.remote.ConcurrentLockedPublisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

public class MockInstanceDiscoveryService implements InstanceDiscoveryService {

    private final Lock lock = spy(ReentrantLock.class);

    private final List<InstanceHostInfo> knownHosts = new ArrayList<>();

    private final ConcurrentLockedPublisher<InstanceHostInfo> onDiscoveryPublisher = spy(new ConcurrentLockedPublisher<>(lock));

    private final ConcurrentLockedPublisher<InstanceHostInfo> onUndiscoveryPublisher = spy(new ConcurrentLockedPublisher<>(lock));

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public Collection<InstanceHostInfo> getKnownHosts() {
        try {
            lock.lock();
            return new ArrayList<>(knownHosts);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Subscription subscribeToDiscovery(Consumer<InstanceHostInfo> instanceHostInfoConsumer) {
        return getOnDiscoveryPublisher().subscribe(instanceHostInfoConsumer);
    }

    @Override
    public Subscription subscribeToUndiscovery(Consumer<InstanceHostInfo> instanceHostInfoConsumer) {
        return getOnUndiscoveryPublisher().subscribe(instanceHostInfoConsumer);
    }

    public Lock getLock() {
        return lock;
    }

    public ConcurrentLockedPublisher<InstanceHostInfo> getOnDiscoveryPublisher() {
        return onDiscoveryPublisher;
    }

    public ConcurrentLockedPublisher<InstanceHostInfo> getOnUndiscoveryPublisher() {
        return onUndiscoveryPublisher;
    }

    public void addHosts(final String ... connectAddresses) {
        try {
            lock.lock();
            for (final String connectAddress : connectAddresses) addHost(connectAddress);
        } finally {
            lock.unlock();
        }
    }

    public void addHost(final String connectAddress) {
        try {
            lock.lock();
            final InstanceHostInfo instanceHostInfo = new MockInstanceHostInfo(connectAddress);
            knownHosts.add(instanceHostInfo);
            getOnDiscoveryPublisher().publish(instanceHostInfo);
        } finally {
            lock.unlock();
        }
    }

    public void removeHosts(final String ... connectAddresses) {
        try {
            lock.lock();
            for (final String connectAddress : connectAddresses) removeHost(connectAddress);
        } finally {
            lock.unlock();
        }
    }

    public void removeHost(final String connectAddress) {
        try {
            lock.lock();
            final InstanceHostInfo instanceHostInfo = new MockInstanceHostInfo(connectAddress);
            knownHosts.remove(instanceHostInfo);
            getOnUndiscoveryPublisher().publish(instanceHostInfo);
        } finally {
            lock.unlock();
        }
    }

    public void resetInternal() {
        reset(lock, onDiscoveryPublisher, onUndiscoveryPublisher);
    }

    private static class MockInstanceHostInfo implements InstanceHostInfo {

        private final String connectAddress;

        public MockInstanceHostInfo(String connectAddress) {
            this.connectAddress = connectAddress;
        }

        @Override
        public String getConnectAddress() {
            return connectAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InstanceHostInfo)) return false;
            InstanceHostInfo that = (InstanceHostInfo) o;
            return Objects.equals(getConnectAddress(), that.getConnectAddress());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getConnectAddress());
        }

    }

}
