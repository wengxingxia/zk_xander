package com.xander.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * Description: 检测节点是否存在
 *
 * @author Xander
 * datetime: 2020-12-28 11:02
 */
public class ZkExist implements Watcher {

    //倒数门闩，计数为1
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zk = null;

    public static void main(String[] args) throws Exception {
        zk = new ZooKeeper("k8smaster:2181",//zookeeper服务器
                5000, //会话超时时间
                new ZkExist());// 注册Watcher
        //阻塞，直到countdown计数等于0
        countDownLatch.await();
        String path = "/zk-sync";

        // 同步检测，如果节点不存在，则返回null，并对该 path 进行事件监听
        Stat stat = zk.exists(path, true);
        if (stat != null) {
            System.out.println(stat.getCzxid() + "," +
                    stat.getMzxid() + "," +
                    stat.getVersion());
        } else {
            System.out.println("节点：" + path + " 不存在");
        }

        zk.create(path, "aaa".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        zk.setData(path, "123".getBytes(), -1);

        zk.create(path + "/c1", "bbb".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        zk.delete(path + "/c1", -1);

        zk.delete(path, -1);

        Thread.sleep(Integer.MAX_VALUE);
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            if (Event.KeeperState.SyncConnected == event.getState()) {
                if (Event.EventType.None == event.getType() && null == event.getPath()) {
                    countDownLatch.countDown();
                } else if (Event.EventType.NodeCreated == event.getType()) {
                    System.out.println("Node(" + event.getPath() + ")Created");
                    zk.exists(event.getPath(), true);
                } else if (Event.EventType.NodeDeleted == event.getType()) {
                    System.out.println("Node(" + event.getPath() + ")Deleted");
                    zk.exists(event.getPath(), true);
                } else if (Event.EventType.NodeDataChanged == event.getType()) {
                    System.out.println("Node(" + event.getPath() + ")DataChanged");
                    zk.exists(event.getPath(), true);
                }
            }
        } catch (Exception e) {
        }
    }
}

