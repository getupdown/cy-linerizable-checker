package cn.cy.concurrent.lock.zookeeper;

import org.apache.zookeeper.KeeperException;

/**
 *
 */
public class ZKException extends RuntimeException {

    private KeeperException keeperException;

    public ZKException(KeeperException keeperException) {
        this.keeperException = keeperException;
    }
}
