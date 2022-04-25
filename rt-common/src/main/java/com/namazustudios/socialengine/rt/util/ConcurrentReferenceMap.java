package com.namazustudios.socialengine.rt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Thread.interrupted;

/**
 * A reference map backed by either a {@link WeakReference} or {@link SoftReference}
 */
public abstract class ConcurrentReferenceMap {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentReferenceMap.class);

    private static final Thread vacuum;

    private static final ReferenceQueue<Object> references;

    private static final Map<Reference<?>, Runnable> collections;

    static {
        references = new ReferenceQueue<>();
        collections = new ConcurrentHashMap<>();
        vacuum = new Thread(ConcurrentReferenceMap::vacuum);
        vacuum.setName(ConcurrentReferenceMap.class.getSimpleName() + " vacuum.");
        vacuum.setDaemon(true);
        vacuum.start();
    }

    private static void vacuum() {
        try {

            logger.info("Starting vacuum thread.");

            while(!interrupted()) {
                try {
                    final Reference<?> ref = references.remove();
                    final Runnable collection = collections.remove(ref);
                    if (collection != null) collection.run();
                } catch (InterruptedException ex) {
                    logger.info("Interrupted.  Exiting.", ex);
                    break;
                } catch (Exception ex) {
                    logger.error("Caught exception running cleanup routine.", ex);
                }
            }
        } finally {
            logger.info("Vacuum thread exiting.");
        }
    }

    public static class Builder<K, V> {

        private Consumer<K> cleanup = v -> {};

        private BiFunction<V, ReferenceQueue<Object>, Reference<V>> referenceCtor = WeakReference::new;

        public Builder<K, V> withCleanup(final Consumer<K> cleanup) {
            this.cleanup = this.cleanup.andThen(cleanup);
            return this;
        }

        public Builder<K, V> withWeakRef() {
            referenceCtor = WeakReference::new;
            return this;
        }

        public Builder<K, V> withSoftRef() {
            referenceCtor = SoftReference::new;
            return this;
        }

        public ConcurrentMap<K, V> build() {
            final var delegate = new ConcurrentHashMap<K, ReferenceWrapper<V>>();
            return new ConcurrentMapWrapper<>(delegate, referenceCtor, cleanup);
        }

    }

    private static class ConcurrentMapWrapper<K, V> implements ConcurrentMap<K, V> {

        private final ConcurrentMap<K, ReferenceWrapper<V>> delegate;

        private final ReferenceConstructor<K, V> referenceCtor;

        private ConcurrentMapWrapper(
                final ConcurrentMap<K, ReferenceWrapper<V>> delegate,
                final BiFunction<V, ReferenceQueue<Object>, Reference<V>> referenceCtor,
                final Consumer<K> cleanup) {

            this.delegate = delegate;

            final Consumer<K> remove = delegate::remove;
            final Consumer<K> doCleanup = remove.andThen(cleanup);

            this.referenceCtor = (key, value, ref) -> {
                final var reference = referenceCtor.apply(value, references);
                collections.put(reference, () -> doCleanup.accept(key));
                return reference;
            };

        }

        @Override
        public V getOrDefault(final Object key, final V defaultValue) {
            final var result = delegate.getOrDefault(key, null);
            return result == null ? null : result.get();
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            delegate.forEach((k, ref) -> {
                final var v = ref.get();
                if (v != null) action.accept(k, v);
            });
        }

        @Override
        public V putIfAbsent(K key, V value) {

            final var existing = delegate.putIfAbsent(
                key,
                referenceCtor.wrapper(key, value)
            );

            return existing == null ? null : existing.get();

        }

        @Override
        public boolean remove(final Object key, final Object value) {
            return delegate.remove(key, value);
        }

        @Override
        public boolean replace(final K key, final V oldValue, final V newValue) {
            final var oldValueWrapper = referenceCtor.wrapper(key, oldValue);
            final var newValueWrapper = referenceCtor.wrapper(key, newValue);
            return delegate.replace(key, oldValueWrapper, newValueWrapper);
        }

        @Override
        public V replace(final K key, final V value) {
            final var valueWrapper = referenceCtor.wrapper(key, value);
            final var reference = delegate.replace(key, valueWrapper);
            return reference == null ? null : reference.get();
        }

        @Override
        public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> mappingFunction) {
            delegate.replaceAll((k, vWrapper) -> {
                final var value = mappingFunction.apply(k, vWrapper.get());
                return value == null ? null : referenceCtor.wrapper(k, value);
            });
        }

        @Override
        public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {

            final var reference = delegate.computeIfAbsent(key, k -> {
                final var value = mappingFunction.apply(k);
                return value == null ? null : referenceCtor.wrapper(key, value);
            });

            return reference == null ? null : reference.get();

        }

        @Override
        public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {

            final var reference = delegate.computeIfPresent(key, (k, vWrapper) -> {
                final var result = remappingFunction.apply(k, vWrapper.get());
                return result == null ? null : referenceCtor.wrapper(k, result);
            });

            return reference == null ? null : reference.get();

        }

        @Override
        public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {

            final var reference = delegate.compute(key, (k, vWrapper) -> {
                final var result = remappingFunction.apply(k, vWrapper == null ? null : vWrapper.get());
                return result == null ? null : referenceCtor.wrapper(k, result);
            });

            return reference == null ? null : reference.get();

        }

        @Override
        public V merge(final K key, final V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {

            final var vWrapped = referenceCtor.wrapper(key, value);
            final var reference = delegate.merge(
                key,
                vWrapped,
                (v0, v1) -> {

                    final var result = remappingFunction.apply(
                        v0 == null ? null : v0.get(),
                        v1 == null ? null : v1.get()
                    );

                    return result == null ? null : referenceCtor.wrapper(key, result);

            });

            return reference == null ? null : reference.get();

        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean containsKey(final Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public V get(final Object key) {
            final var reference =  delegate.get(key);
            return reference == null ? null : reference.get();
        }

        @Override
        public V put(final K key, final V value) {
            final var wrapper = referenceCtor.wrapper(key, value);
            final var result = delegate.put(key, wrapper);
            return result == null ? null : result.get();
        }

        @Override
        public V remove(final Object key) {
            final var result = delegate.remove(key);
            if (result != null) collections.remove(result.reference);
            return result == null ? null : result.get();
        }

        @Override
        public void putAll(final Map<? extends K, ? extends V> m) {
            m.forEach((k, v) -> delegate.put(k, referenceCtor.wrapper(k, v)));
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public Set<K> keySet() {
            return delegate.keySet();
        }

        @Override
        public Collection<V> values() {
            return new AbstractCollection<>() {

                @Override
                public Iterator<V> iterator() {
                    return delegate
                        .values()
                        .stream()
                        .map(ReferenceWrapper::get)
                        .filter(Objects::nonNull)
                        .iterator();
                }

                @Override
                public int size() {
                    return delegate.values().size();
                }

                @Override
                public boolean contains(final Object o) {
                    return delegate.containsValue(o);
                }

                @Override
                public void clear() {
                    delegate.clear();
                }

                @Override
                public boolean isEmpty() {
                    return delegate.isEmpty();
                }

            };
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return new AbstractSet<>() {

                @Override
                public Iterator<Entry<K, V>> iterator() {
                    return delegate.entrySet()
                        .stream()
                        .map(ConcurrentMapWrapper.this::mapEntry)
                        .filter(e -> e.getValue() != null)
                        .iterator();
                }

                @Override
                public void clear() {
                    delegate.clear();
                }

                @Override
                public boolean isEmpty() {
                    return delegate.isEmpty();
                }

                @Override
                public int size() {
                    return delegate.size();
                }

            };
        }

        private Entry<K, V> mapEntry(final Entry<K, ReferenceWrapper<V>> original) {
            return new Entry<>() {
                @Override
                public K getKey() {
                    return original.getKey();
                }

                @Override
                public V getValue() {
                    final var wrapper = original.getValue();
                    return wrapper == null ? null : wrapper.get();
                }

                @Override
                public V setValue(final V value) {
                    final var wrapper = referenceCtor.wrapper(original.getKey(), value);
                    final var result = original.setValue(wrapper);
                    return result == null ? null : result.get();
                }
            };
        }

    }

    private static final class ReferenceWrapper<ReferentT> {

        private final Reference<ReferentT> reference;

        public ReferenceWrapper(final Reference<ReferentT> reference) {
            this.reference = reference;
        }

        public ReferentT get() {
            return reference.get();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || get() == null) return false;
            if (getClass() != o.getClass()) return get() == o;
            final var that = (ReferenceWrapper<?>) o;
            return get() == that.get();
        }

        @Override
        public int hashCode() {
            final var value = get();
            return value == null ? 0 : System.identityHashCode(get());
        }

    }

    @FunctionalInterface
    private interface ReferenceConstructor<KeyT, ValueT> {

        Reference<ValueT> construct(KeyT key, ValueT value, ReferenceQueue<? super Reference<ValueT>> queue);

        default ReferenceWrapper<ValueT> wrapper(final KeyT key, final ValueT value) {
            final var reference = construct(key, value, references);
            return new ReferenceWrapper<>(reference);
        }

    }

}


