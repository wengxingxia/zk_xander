package com.xander.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * Description: 同步创建节点
 *
 * @author Xander
 * datetime: 2020-12-28 11:02
 */
public class ZkCreateSync implements Watcher {

    //倒数门闩，计数为1
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        // 创建一个最基本的ZooKeeper对象实例
        ZooKeeper zookeeper = new ZooKeeper("k8smaster:2181",//zookeeper服务器
                5000, //会话超时时间
                new ZkCreateSync());// 注册Watcher
        System.out.println("当前zookeeper状态：" + zookeeper.getState());
        //阻塞，直到countdown计数等于0
        countDownLatch.await();
        System.out.println("zookeeper 会话建立");
        String path1 = zookeeper.create("/zk-test-ephemeral-",// 节点路径
                // 节点数据
                "1111".getBytes(),
                // 对这个节点的任何操作都不受权限控制
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                // 临时节点
                CreateMode.EPHEMERAL);
        System.out.println("Success create znode: " + path1);

        String path2 = zookeeper.create("/zk-test-ephemeral-",//节点路径
                // 节点数据
                "2222".getBytes(),
                // 对这个节点的任何操作都不受权限控制
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                // 临时有序节点
                CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Success create znode: " + path2);
    }

    public void process(WatchedEvent event) {
        System.out.println("接收到监听事件：" + event);
        if (Event.KeeperState.SyncConnected == event.getState()) {
            // 倒数，计数-1
            countDownLatch.countDown();
        }
    }
}
