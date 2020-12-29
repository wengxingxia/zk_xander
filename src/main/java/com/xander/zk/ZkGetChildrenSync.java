package com.xander.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Description: 同步获取子节点列表
 *
 * @author Xander
 * datetime: 2020-12-28 11:02
 */
public class ZkGetChildrenSync implements Watcher {

    //倒数门闩，计数为1
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zk = null;

    public static void main(String[] args) throws Exception {
        zk = new ZooKeeper("k8smaster:2181",//zookeeper服务器
                5000, //会话超时时间
                new ZkGetChildrenSync());// 注册Watcher
        System.out.println("当前zookeeper状态：" + zk.getState());
        //阻塞，直到countdown计数等于0
        countDownLatch.await();
        System.out.println("zookeeper 会话建立");
        String path = "/zk-parent";
        zk.create(path, "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        // 创建子节点
        zk.create(path + "/c1", "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        List<String> childrenList = zk.getChildren(path, true);
        // 子节点列表
        System.out.println("子节点列表" + childrenList);

        zk.create(path + "/c2", "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        zk.create(path + "/c4", "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        childrenList = zk.getChildren(path, true);
        System.out.println("子节点列表" + childrenList);
        Thread.sleep(Integer.MAX_VALUE);
    }

    public void process(WatchedEvent event) {
        if (Event.KeeperState.SyncConnected == event.getState()) {
            if (Event.EventType.None == event.getType() && null == event.getPath()) {
                countDownLatch.countDown();
            }
        }
    }
}
