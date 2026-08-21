package dev.getelements.elements.rt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class ReentrantReadWriteGuard implements ReadWriteGuard {

    private final Logger logger;

    private final Runnable signal;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final Lock rLock = rwLock.readLock();

    private final Lock wLock = rwLock.writeLock();

    private final Condition wCondition = wLock.newCondition();

    public ReentrantReadWriteGuard() {
        this(LoggerFactory.getLogger(ReentrantReadWriteGuard.class), true);
    }

    public ReentrantReadWriteGuard(final Logger logger, final boolean signaling) {
        this.logger = logger;
        this.signal = signaling ? wCondition::signalAll : () -> {};
    }

    @Override
    public Lock getReadLock() {
        return rLock;
    }

    @Override
    public Lock getWriteLock() {
        return wLock;
    }

    @Override
    public Condition getCondition() {
        return wCondition;
    }

    @Override
    public <T> T computeRO(final Function<Condition, T> op) {
        try {
            rLock.lock();
            return op.apply(wCondition);
        } catch(Exception ex) {
            logger.error("Caught exception in operation.", ex);
            throw ex;
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public <T> T computeRW(final Function<Condition, T> op) {
        try {
            wLock.lock();
            signal.run();
            wCondition.signalAll();
            return op.apply(wCondition);
        } catch(Exception ex) {
            logger.error("Caught exception in operation.", ex);
            throw ex;
        } finally {
            wLock.unlock();
        }
    }

}
