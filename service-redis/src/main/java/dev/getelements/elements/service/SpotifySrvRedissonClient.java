package dev.getelements.elements.service;

import dev.getelements.elements.exception.UnavailableException;
import dev.getelements.elements.rt.util.HostList;
import com.spotify.dns.*;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;

public class SpotifySrvRedissonClient implements RedissonClient {

    private static final Logger logger = LoggerFactory.getLogger(SpotifySrvRedissonClient.class);

    private static final long DNS_LOOKUP_TIMEOUT = 1000;

    private static final long DNS_LOOKUP_POLLING_RATE = 1;

    private static final TimeUnit DNS_LOOKUP_POLLING_RATE_UNITS = SECONDS;

    private static final long RECONNECT_INTERVAL = 5;

    private static final TimeUnit RECONNECT_INTERVAL_UNITS = SECONDS;

    private final DnsSrvWatcher<LookupResult> dnsSrvWatcher;

    private final ScheduledExecutorService connector = newSingleThreadScheduledExecutor();

    private final AtomicReference<Future<?>> pending = new AtomicReference<>();

    private final AtomicReference<RedissonClient> delegate= new AtomicReference<>();

    private SpotifySrvRedissonClient(final DnsSrvWatcher<LookupResult> dnsSrvWatcher,
                                     final ChangeNotifier<LookupResult> changeNotifier,
                                     final Function<Set<String>, Config> configSupplier) {

        this.dnsSrvWatcher = dnsSrvWatcher;

        changeNotifier.setListener(cn -> {

            logger.info("Discovered new Redis Hosts {}", cn.current());

            final var hosts = cn.current()
              .stream()
              .map(this::transform)
              .collect(toSet());

            if (hosts.isEmpty()) {

                final var pending = this.pending.getAndSet(null);
                pending.cancel(false);

                final var outdated = delegate.getAndSet(null);
                safeDispose(outdated);

            } else {
                final var config = configSupplier.apply(hosts);
                refresh(config);
            }

        }, false);

    }

    private String transform(final LookupResult lookupResult) {

        final var host = lookupResult.host().endsWith(".")
            ? lookupResult.host().substring(0, lookupResult.host().length() - 1)
            : lookupResult.host();

        return format("redis://%s:%s", host, lookupResult.port());

    }

    private void refresh(final Config config) {
        final var pending = connector.submit(() -> doRefresh(config));
        final var previous = this.pending.getAndSet(pending);
        if (previous != null) previous.cancel(false);
    }

    private void doRefresh(final Config config) {

        final var previous = this.pending.get();

        try {
            final var updated = Redisson.create(config);
            final var outdated = delegate.getAndSet(updated);
            if (outdated != null) safeDispose(outdated);
        } catch (Exception ex) {

            logger.info("Caught exception connecting new client. Re-attempting connection.", ex);
            final var pending = connector.schedule(() -> refresh(config), RECONNECT_INTERVAL, RECONNECT_INTERVAL_UNITS);

            if (!this.pending.compareAndSet(previous, pending)) {
                pending.cancel(false);
            }

        }
    }

    private void safeDispose(final RedissonClient outdated) {
        try {
            outdated.shutdown();
        } catch (Exception ex) {
            logger.error("Error shutting down old client.", ex);
        }
    }

    @Override
    public <V> RTimeSeries<V> getTimeSeries(String name) {
        return getDelegate().getTimeSeries(name);
    }

    @Override
    public <V> RTimeSeries<V> getTimeSeries(String name, Codec codec) {
        return getDelegate().getTimeSeries(name, codec);
    }

    @Override
    public <K, V> RStream<K, V> getStream(String name) {
        return getDelegate().getStream(name);
    }

    @Override
    public <K, V> RStream<K, V> getStream(String name, Codec codec) {
        return getDelegate().getStream(name, codec);
    }

    @Override
    public RRateLimiter getRateLimiter(String name) {
        return getDelegate().getRateLimiter(name);
    }

    @Override
    public RBinaryStream getBinaryStream(String name) {
        return getDelegate().getBinaryStream(name);
    }

    @Override
    public <V> RGeo<V> getGeo(String name) {
        return getDelegate().getGeo(name);
    }

    @Override
    public <V> RGeo<V> getGeo(String name, Codec codec) {
        return getDelegate().getGeo(name, codec);
    }

    @Override
    public <V> RSetCache<V> getSetCache(String name) {
        return getDelegate().getSetCache(name);
    }

    @Override
    public <V> RSetCache<V> getSetCache(String name, Codec codec) {
        return getDelegate().getSetCache(name, codec);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec) {
        return getDelegate().getMapCache(name, codec);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec, MapOptions<K, V> options) {
        return getDelegate().getMapCache(name, codec, options);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name) {
        return getDelegate().getMapCache(name);
    }

    @Override
    public <K, V> RMapCache<K, V> getMapCache(String name, MapOptions<K, V> options) {
        return getDelegate().getMapCache(name, options);
    }

    @Override
    public <V> RBucket<V> getBucket(String name) {
        return getDelegate().getBucket(name);
    }

    @Override
    public <V> RBucket<V> getBucket(String name, Codec codec) {
        return getDelegate().getBucket(name, codec);
    }

    @Override
    public RBuckets getBuckets() {
        return getDelegate().getBuckets();
    }

    @Override
    public RBuckets getBuckets(Codec codec) {
        return getDelegate().getBuckets(codec);
    }

    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name) {
        return getDelegate().getHyperLogLog(name);
    }

    @Override
    public <V> RHyperLogLog<V> getHyperLogLog(String name, Codec codec) {
        return getDelegate().getHyperLogLog(name, codec);
    }

    @Override
    public <V> RList<V> getList(String name) {
        return getDelegate().getList(name);
    }

    @Override
    public <V> RList<V> getList(String name, Codec codec) {
        return getDelegate().getList(name, codec);
    }

    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name) {
        return getDelegate().getListMultimap(name);
    }

    @Override
    public <K, V> RListMultimap<K, V> getListMultimap(String name, Codec codec) {
        return getDelegate().getListMultimap(name, codec);
    }

    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name) {
        return getDelegate().getListMultimapCache(name);
    }

    @Override
    public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name, Codec codec) {
        return getDelegate().getListMultimapCache(name, codec);
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, LocalCachedMapOptions<K, V> options) {
        return getDelegate().getLocalCachedMap(name, options);
    }

    @Override
    public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec, LocalCachedMapOptions<K, V> options) {
        return getDelegate().getLocalCachedMap(name, codec, options);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name) {
        return getDelegate().getMap(name);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, MapOptions<K, V> options) {
        return getDelegate().getMap(name, options);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec) {
        return getDelegate().getMap(name, codec);
    }

    @Override
    public <K, V> RMap<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
        return getDelegate().getMap(name, codec, options);
    }

    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name) {
        return getDelegate().getSetMultimap(name);
    }

    @Override
    public <K, V> RSetMultimap<K, V> getSetMultimap(String name, Codec codec) {
        return getDelegate().getSetMultimap(name, codec);
    }

    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name) {
        return getDelegate().getSetMultimapCache(name);
    }

    @Override
    public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name, Codec codec) {
        return getDelegate().getSetMultimapCache(name, codec);
    }

    @Override
    public RSemaphore getSemaphore(String name) {
        return getDelegate().getSemaphore(name);
    }

    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
        return getDelegate().getPermitExpirableSemaphore(name);
    }

    @Override
    public RLock getLock(String name) {
        return getDelegate().getLock(name);
    }

    @Override
    public RLock getMultiLock(RLock... locks) {
        return getDelegate().getMultiLock(locks);
    }

    @Override
    @Deprecated
    public RLock getRedLock(RLock... locks) {
        return getDelegate().getRedLock(locks);
    }

    @Override
    public RLock getFairLock(String name) {
        return getDelegate().getFairLock(name);
    }

    @Override
    public RReadWriteLock getReadWriteLock(String name) {
        return getDelegate().getReadWriteLock(name);
    }

    @Override
    public <V> RSet<V> getSet(String name) {
        return getDelegate().getSet(name);
    }

    @Override
    public <V> RSet<V> getSet(String name, Codec codec) {
        return getDelegate().getSet(name, codec);
    }

    @Override
    public <V> RSortedSet<V> getSortedSet(String name) {
        return getDelegate().getSortedSet(name);
    }

    @Override
    public <V> RSortedSet<V> getSortedSet(String name, Codec codec) {
        return getDelegate().getSortedSet(name, codec);
    }

    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name) {
        return getDelegate().getScoredSortedSet(name);
    }

    @Override
    public <V> RScoredSortedSet<V> getScoredSortedSet(String name, Codec codec) {
        return getDelegate().getScoredSortedSet(name, codec);
    }

    @Override
    public RLexSortedSet getLexSortedSet(String name) {
        return getDelegate().getLexSortedSet(name);
    }

    @Override
    public RTopic getTopic(String name) {
        return getDelegate().getTopic(name);
    }

    @Override
    public RTopic getTopic(String name, Codec codec) {
        return getDelegate().getTopic(name, codec);
    }

    @Override
    public RReliableTopic getReliableTopic(String name) {
        return getDelegate().getReliableTopic(name);
    }

    @Override
    public RReliableTopic getReliableTopic(String name, Codec codec) {
        return getDelegate().getReliableTopic(name, codec);
    }

    @Override
    public RPatternTopic getPatternTopic(String pattern) {
        return getDelegate().getPatternTopic(pattern);
    }

    @Override
    public RPatternTopic getPatternTopic(String pattern, Codec codec) {
        return getDelegate().getPatternTopic(pattern, codec);
    }

    @Override
    public <V> RQueue<V> getQueue(String name) {
        return getDelegate().getQueue(name);
    }

    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name) {
        return getDelegate().getTransferQueue(name);
    }

    @Override
    public <V> RTransferQueue<V> getTransferQueue(String name, Codec codec) {
        return getDelegate().getTransferQueue(name, codec);
    }

    @Override
    public <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> destinationQueue) {
        return getDelegate().getDelayedQueue(destinationQueue);
    }

    @Override
    public <V> RQueue<V> getQueue(String name, Codec codec) {
        return getDelegate().getQueue(name, codec);
    }

    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name) {
        return getDelegate().getRingBuffer(name);
    }

    @Override
    public <V> RRingBuffer<V> getRingBuffer(String name, Codec codec) {
        return getDelegate().getRingBuffer(name, codec);
    }

    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name) {
        return getDelegate().getPriorityQueue(name);
    }

    @Override
    public <V> RPriorityQueue<V> getPriorityQueue(String name, Codec codec) {
        return getDelegate().getPriorityQueue(name, codec);
    }

    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name) {
        return getDelegate().getPriorityBlockingQueue(name);
    }

    @Override
    public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name, Codec codec) {
        return getDelegate().getPriorityBlockingQueue(name, codec);
    }

    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name) {
        return getDelegate().getPriorityBlockingDeque(name);
    }

    @Override
    public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name, Codec codec) {
        return getDelegate().getPriorityBlockingDeque(name, codec);
    }

    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name) {
        return getDelegate().getPriorityDeque(name);
    }

    @Override
    public <V> RPriorityDeque<V> getPriorityDeque(String name, Codec codec) {
        return getDelegate().getPriorityDeque(name, codec);
    }

    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name) {
        return getDelegate().getBlockingQueue(name);
    }

    @Override
    public <V> RBlockingQueue<V> getBlockingQueue(String name, Codec codec) {
        return getDelegate().getBlockingQueue(name, codec);
    }

    @Override
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name) {
        return getDelegate().getBoundedBlockingQueue(name);
    }

    @Override
    public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name, Codec codec) {
        return getDelegate().getBoundedBlockingQueue(name, codec);
    }

    @Override
    public <V> RDeque<V> getDeque(String name) {
        return getDelegate().getDeque(name);
    }

    @Override
    public <V> RDeque<V> getDeque(String name, Codec codec) {
        return getDelegate().getDeque(name, codec);
    }

    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name) {
        return getDelegate().getBlockingDeque(name);
    }

    @Override
    public <V> RBlockingDeque<V> getBlockingDeque(String name, Codec codec) {
        return getDelegate().getBlockingDeque(name, codec);
    }

    @Override
    public RAtomicLong getAtomicLong(String name) {
        return getDelegate().getAtomicLong(name);
    }

    @Override
    public RAtomicDouble getAtomicDouble(String name) {
        return getDelegate().getAtomicDouble(name);
    }

    @Override
    public RLongAdder getLongAdder(String name) {
        return getDelegate().getLongAdder(name);
    }

    @Override
    public RDoubleAdder getDoubleAdder(String name) {
        return getDelegate().getDoubleAdder(name);
    }

    @Override
    public RCountDownLatch getCountDownLatch(String name) {
        return getDelegate().getCountDownLatch(name);
    }

    @Override
    public RBitSet getBitSet(String name) {
        return getDelegate().getBitSet(name);
    }

    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name) {
        return getDelegate().getBloomFilter(name);
    }

    @Override
    public <V> RBloomFilter<V> getBloomFilter(String name, Codec codec) {
        return getDelegate().getBloomFilter(name, codec);
    }

    @Override
    public RIdGenerator getIdGenerator(String name) {
        return getDelegate().getIdGenerator(name);
    }

    @Override
    public RScript getScript() {
        return getDelegate().getScript();
    }

    @Override
    public RScript getScript(Codec codec) {
        return getDelegate().getScript(codec);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name) {
        return getDelegate().getExecutorService(name);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, ExecutorOptions options) {
        return getDelegate().getExecutorService(name, options);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec) {
        return getDelegate().getExecutorService(name, codec);
    }

    @Override
    public RScheduledExecutorService getExecutorService(String name, Codec codec, ExecutorOptions options) {
        return getDelegate().getExecutorService(name, codec, options);
    }

    @Override
    public RRemoteService getRemoteService() {
        return getDelegate().getRemoteService();
    }

    @Override
    public RRemoteService getRemoteService(Codec codec) {
        return getDelegate().getRemoteService(codec);
    }

    @Override
    public RRemoteService getRemoteService(String name) {
        return getDelegate().getRemoteService(name);
    }

    @Override
    public RRemoteService getRemoteService(String name, Codec codec) {
        return getDelegate().getRemoteService(name, codec);
    }

    @Override
    public RTransaction createTransaction(TransactionOptions options) {
        return getDelegate().createTransaction(options);
    }

    @Override
    public RBatch createBatch(BatchOptions options) {
        return getDelegate().createBatch(options);
    }

    @Override
    public RBatch createBatch() {
        return getDelegate().createBatch();
    }

    @Override
    public RKeys getKeys() {
        return getDelegate().getKeys();
    }

    @Override
    public RLiveObjectService getLiveObjectService() {
        return getDelegate().getLiveObjectService();
    }

    private boolean stopWatching() {

        connector.shutdown();

        try {
            dnsSrvWatcher.close();
        } catch (IOException e) {
            logger.error("Caught error shutting down watcher.", e);
            return false;
        }

        try {
            return connector.awaitTermination(1, MINUTES);
        } catch (InterruptedException ex) {
            logger.info("Interrupted while shutting down.", ex);
            return false;
        }

    }

    @Override
    public void shutdown() {

        if (!stopWatching()) {
            logger.error("Unclean shutdown.");
        }

        final var delegate = this.delegate.getAndSet(null);
        if (delegate != null) delegate.shutdown();

    }

    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {

        if (!stopWatching()) {
            logger.error("Unclean shutdown.");
        }

        final var delegate = this.delegate.getAndSet(null);
        if (delegate != null) delegate.shutdown(quietPeriod, timeout, unit);

    }

    @Override
    public Config getConfig() {
        return getDelegate().getConfig();
    }

    @Override
    public <T extends BaseRedisNodes> T getRedisNodes(RedisNodes<T> nodes) {
        return getDelegate().getRedisNodes(nodes);
    }

    @Override
    @Deprecated
    public NodesGroup<Node> getNodesGroup() {
        return getDelegate().getNodesGroup();
    }

    @Override
    @Deprecated
    public ClusterNodesGroup getClusterNodesGroup() {
        return getDelegate().getClusterNodesGroup();
    }

    @Override
    public boolean isShutdown() {
        return getDelegate().isShutdown();
    }

    @Override
    public boolean isShuttingDown() {
        return getDelegate().isShuttingDown();
    }

    @Override
    public String getId() {
        return getDelegate().getId();
    }

    private RedissonClient getDelegate() {
        final var del = delegate.get();
        if (del == null) throw new UnavailableException("Temporarily unavailble.");
        return del;
    }

    public static class Builder {

        private String srvQuery = null;

        private final HostList hostList = new HostList();

        private Function<Set<String>, Config> configSupplier = hosts -> {
            final var conf = new Config();
            conf.useReplicatedServers().addNodeAddress(hosts.toArray(String[]::new));
            return conf;
        };

        public Builder withQuery(final String query) {
            requireNonNull(query, "query");
            srvQuery = query;
            return this;
        }

        public Builder withServers(final String hosts) {
            hostList.with(hosts);
            return this;
        }

        public Builder withConfigSupplier(final Function<Set<String>, Config> configSupplier) {
            requireNonNull(configSupplier, "configSupplier");
            this.configSupplier = configSupplier;
            return this;
        }

        public SpotifySrvRedissonClient build() {

            if (srvQuery == null) throw new IllegalStateException("query not specified.");

            final var builder = hostList
                .get()
                .map(hosts -> DnsSrvResolvers.newBuilder().servers(hosts))
                .orElseGet(DnsSrvResolvers::newBuilder);

            final var dnsSrvResolver = builder
                .cachingLookups(true)
                .dnsLookupTimeoutMillis(DNS_LOOKUP_TIMEOUT)
                .build();

            final var dnsSrvWatcher = DnsSrvWatchers.newBuilder(dnsSrvResolver)
                .polling(DNS_LOOKUP_POLLING_RATE, DNS_LOOKUP_POLLING_RATE_UNITS)
                .withErrorHandler(((fqdn, ex) -> logger.error("Error in SRV lookup for {}", fqdn, ex)))
                .build();

            final var changeNotifier = dnsSrvWatcher.watch(srvQuery);

            return new SpotifySrvRedissonClient(dnsSrvWatcher, changeNotifier, configSupplier);

        }

    }

}
