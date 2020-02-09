package cn.cy.concurrent.lock.zookeeper;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ZkCliTest {

    public static final String BASE_PATH = "/zkCliTest";

    public static ZkCli zkCli;

    static {
        try {
            zkCli = new ZkCli("118.31.4.42:2182" + BASE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void before() {
        zkCli.clearTree("/");
        zkCli.createNode("/", "node", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @AfterClass
    public static void after() {
        zkCli.clearTree("/");
    }

    @Test
    public void createNode() {

        String res = zkCli.createNode("/test1", "data1", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        System.out.println(res);

        Assert.assertNotNull(res);

        zkCli.removeNode("/test1", -1);
    }

    @Test
    public void getChildren() {

        Preconditions.checkNotNull(zkCli.createNode("/test1", "data1", ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT));

        List<String> childrenList = Lists.newArrayList("1", "2", "3");

        for (String s : childrenList) {
            Preconditions.checkNotNull(zkCli.createNode("/test1/" + s, "node", ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT));
        }

        List<String> st = zkCli.getChildren("/test1", null);

        for (int i = 0; i < st.size(); i++) {
            Assert.assertEquals(st.get(i), childrenList.get(i));
        }

    }

    @Test
    public void attachWatcher() {

        Preconditions.checkNotNull(zkCli.createNode("/test1", "data1", ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT));

        Preconditions.checkState(zkCli.attachWatcher("/test1", new Watcher() {
            @Override
            public void process(WatchedEvent event) {

            }
        }));
    }

    /**
     * 构造场景
     * <p>
     * L1, F1, F2
     * zkCli只连接F2
     * <p>
     * 1. F2崩溃
     * 2. L1上删除一个节点
     * 3. F2恢复
     * 4. F2读取被删除节点的数据
     * <p>
     * 结论: 发生connection loss
     */
    @Test
    public void attachWatcherToNonExistBecauseOfConnectionLoss() {

        zkCli.createNode("/test1", "data", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // for breakpoint
        System.out.println("");

        System.out.println(zkCli.attachWatcher("/test1", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("for debug");
            }
        }));
    }

    /**
     * 构造场景
     * <p>
     * zkClient同时连 L1 F1 F2
     * 连着的那个挂掉
     * 然后看是否会重连其他的
     * <p>
     * 结论: 会重连其他的
     */
    @Test
    public void testMultiClient() throws IOException, InterruptedException {
        zkCli = new ZkCli("118.31.4.42:2181,118.31.4.42:2182,118.31.4.42:2183");

        zkCli.attachWatcher("/", null);
    }

    @Test
    public void testAttachOnNotExist() throws IOException, InterruptedException {
        Assert.assertFalse(zkCli.attachWatcher("/test", null));
    }

    @Test
    public void testAttachOnExistingNode() {

        zkCli.createNode("/testExist", "test", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        Assert.assertTrue(zkCli.attachWatcher("/testExist", null));

    }

    @Test
    public void testSyncBySync() throws InterruptedException {
        zkCli.syncBySync("/");
    }
}