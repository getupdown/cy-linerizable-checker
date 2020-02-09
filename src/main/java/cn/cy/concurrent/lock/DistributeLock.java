package cn.cy.concurrent.lock;

import java.util.concurrent.TimeUnit;

/**
 * Distribute Lock
 */
public interface DistributeLock {

    /**
     * interruptable acquire the lock
     *
     * @throws InterruptedException
     */
    void acquire() throws InterruptedException;

    /**
     * interruptable acquire the lock with timeout
     *
     * @param timeout
     * @param timeUnit
     *
     * @throws InterruptedException
     */
    void acquire(long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * release the lock
     */
    void release();

}
