package cn.cy.concurrent.lock.callback;

import java.util.concurrent.locks.LockSupport;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * TryingAcquire Watcher
 */
public class TryingAcquireWatcher implements Watcher {

    private static Logger logger = LoggerFactory.getLogger(TryingAcquireWatcher.class);

    private Thread tryingAcquireLockThread;

    public TryingAcquireWatcher(Thread tryingAcquireLockThread) {
        this.tryingAcquireLockThread = tryingAcquireLockThread;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

        logger.info("receive watchedEvent : {}", JSON.toJSONString(watchedEvent));

        switch (watchedEvent.getType()) {
            // 前驱节点删除
            case NodeDeleted:
                LockSupport.unpark(tryingAcquireLockThread);
            default:
                logger.warn("other watchEvent Type! type : {}", watchedEvent.getType());
        }

    }
}
