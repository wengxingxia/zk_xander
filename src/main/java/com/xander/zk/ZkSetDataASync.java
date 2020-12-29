package com.xander.zk;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * Description: 异步设置数据
 *
 * @author Xander
 * datetime: 2020-12-28 11:02
 */
public class ZkSetDataASync implements Watcher {

    //倒数门闩，计数为1
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zk = null;
    private static Stat stat = new Stat();

    public static void main(String[] args) throws Exception {
        zk = new ZooKeeper("k8smaster:2181",//zookeeper服务器
                5000, //会话超时时间
                new ZkSetDataASync());// 注册Watcher
        //阻塞，直到countdown计数等于0
        countDownLatch.await();
        String path = "/zk-async";

        zk.create(path, "123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        zk.setData(path, "456".getBytes(), -1, new IStatCallback(), null);
        System.out.println("getData: " + new String(zk.getData(path, true, stat)));

        Thread.sleep(Integer.MAX_VALUE);
    }

    @Override
    public void process(WatchedEvent event) {
        if (Event.KeeperState.SyncConnected == event.getState()) {
            if (Event.EventType.None == event.getType() && null == event.getPath()) {
                countDownLatch.countDown();
            }
        }
    }
}

class IStatCallback implements AsyncCallback.StatCallback {
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (rc == 0) {
            System.out.println("SUCCESS");
        }
    }
}