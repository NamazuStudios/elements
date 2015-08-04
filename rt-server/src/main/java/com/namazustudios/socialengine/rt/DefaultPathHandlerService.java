package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.NotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by patricktwohig on 8/4/15.
 */
public class DefaultPathHandlerService implements PathHandlerService {

    private final Map<PathHandlerKey, PathHandler<?>> pathHandlerMap = new ConcurrentHashMap<>();

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    private final Lock r = reentrantReadWriteLock.readLock();

    private final Lock w = reentrantReadWriteLock.writeLock();

    @Override
    public PathHandler<?> getPathHandler(final RequestHeader requestHeader) {

        r.lock();

        final String path = requestHeader.getPath();
        final String method = requestHeader.getMethod();

        try {

            final PathHandlerKey pathHandlerKey = new PathHandlerKey(path, method);
            final PathHandler<?> pathHandler = pathHandlerMap.get(pathHandlerKey);

            if (pathHandler == null) {
                throw new NotFoundException(path + ":" + method + " not found.");
            }

            return pathHandler;

        } finally {
            r.unlock();
        }

    }

    @Override
    public <PayloadT> void addPathHandler(final PathHandler<PayloadT> handler, final String path, final String method) {

        w.lock();

        try {
            final PathHandlerKey pathHandlerKey = new PathHandlerKey(path, method);
            pathHandlerMap.put(pathHandlerKey, handler);
        } finally {
            w.unlock();
        }

    }

    private static class PathHandlerKey {

        private final String path;

        private final String method;

        public PathHandlerKey(String path, String method) {

            if (path == null) {
                throw new IllegalArgumentException();
            }

            if (method == null) {
                throw new IllegalArgumentException();
            }

            this.path = path;
            this.method = method;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PathHandlerKey)) return false;

            PathHandlerKey that = (PathHandlerKey) o;

            if (!path.equals(that.path)) return false;
            return method.equals(that.method);

        }

        @Override
        public int hashCode() {
            int result = path.hashCode();
            result = 31 * result + method.hashCode();
            return result;
        }
    }

}
