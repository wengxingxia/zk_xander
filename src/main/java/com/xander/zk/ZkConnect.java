package com.xander.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * Description: 创建连接，创建一个最基本的ZooKeeper对象实例
 *
 * @author Xander
 * datetime: 2020-12-28 11:02
 */
public class ZkConnect implements Watcher {

    //倒数门闩，计数为1
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        // 创建一个最基本的ZooKeeper对象实例
        ZooKeeper zookeeper = new ZooKeeper("k8smaster:2181",//zookeeper服务器
                5000, //会话超时时间
                new ZkConnect());
        System.out.println("当前zookeeper状态：" + zookeeper.getState());
        try {
            //阻塞，直到countdown计数等于0
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        System.out.println("zookeeper 会话建立");
    }

    public void process(WatchedEvent event) {
        System.out.println("接收到监听事件：" + event);
        if (Event.KeeperState.SyncConnected == event.getState()) {
            // 倒数，计数-1
            countDownLatch.countDown();
        }
    }
}
