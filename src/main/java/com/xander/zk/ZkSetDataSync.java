package com.xander.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * Description: 同步设置数据
 *
 * @author Xander
 * datetime: 2020-12-28 11:02
 */
public class ZkSetDataSync implements Watcher {

    //倒数门闩，计数为1
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zk = null;
    private static Stat stat = new Stat();

    public static void main(String[] args) throws Exception {
        zk = new ZooKeeper("k8smaster:2181",//zookeeper服务器
                5000, //会话超时时间
                new ZkSetDataSync());// 注册Watcher
        //阻塞，直到countdown计数等于0
        countDownLatch.await();
        String path = "/zk-sync";
        zk.create(path, "123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println("getData: " + new String(zk.getData(path, true, stat)));
        System.out.println(stat.getCzxid() + "," +
                stat.getMzxid() + "," +
                stat.getVersion());
        // 如果版本号是-1，就是告诉ZooKeeper服务器，客户端需要基于数据的最新版本进行更新操作
        stat = zk.setData(path, "456".getBytes(), -1);
        System.out.println(stat.getCzxid() + "," +
                stat.getMzxid() + "," +
                stat.getVersion());
        System.out.println("getData: " + new String(zk.getData(path, true, stat)));

        // 获取 stat 对应版本的数据
        stat = zk.setData(path, "789".getBytes(), stat.getVersion());
        System.out.println(stat.getCzxid() + "," +
                stat.getMzxid() + "," +
                stat.getVersion());
        System.out.println("getData: " + new String(zk.getData(path, true, stat)));
        try {
            zk.setData(path, "000".getBytes(), stat.getVersion());
        } catch (KeeperException e) {
            System.out.println("Error: " + e.code() + "," + e.getMessage());
        }
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

