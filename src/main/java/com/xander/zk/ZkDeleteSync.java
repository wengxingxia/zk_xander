package com.xander.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * Description: 同步删除节点
 *
 * @author Xander
 * datetime: 2020-12-28 11:02
 */
public class ZkDeleteSync implements Watcher {

    //倒数门闩，计数为1
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        // 创建一个最基本的ZooKeeper对象实例
        ZooKeeper zookeeper = new ZooKeeper("k8smaster:2181",//zookeeper服务器
                5000, //会话超时时间
                new ZkDeleteSync());// 注册Watcher
        System.out.println("当前zookeeper状态：" + zookeeper.getState());
        //阻塞，直到countdown计数等于0
        countDownLatch.await();
        System.out.println("zookeeper 会话建立");
        String path = "/zk-del";
        path = zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println("建立临时节点: " + path);
        zookeeper.delete(path, -1);
        System.out.println("删除临时节点: " + path);
    }

    public void process(WatchedEvent event) {
        if (Event.KeeperState.SyncConnected == event.getState()) {
            if (Event.EventType.None == event.getType() && null == event.getPath()) {
                countDownLatch.countDown();
            }
        }
    }
}
