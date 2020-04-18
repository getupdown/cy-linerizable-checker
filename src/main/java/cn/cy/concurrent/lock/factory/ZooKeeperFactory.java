package cn.cy.concurrent.lock.factory;

import java.util.function.Consumer;

import org.apache.zookeeper.ZooKeeper;

/**
 * ZooKeeperFactory
 */
public interface ZooKeeperFactory {

    /**
     * 获取zooKeeperClient实例
     *
     * @return
     * @param onCreateNewCallback
     */
    ZooKeeper getZooKeeperClientInstance(Consumer<ZooKeeper> onCreateNewCallback);

}
